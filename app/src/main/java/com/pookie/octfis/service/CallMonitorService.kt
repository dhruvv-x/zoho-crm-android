package com.pookie.octfis.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.app.NotificationCompat
import com.pookie.octfis.R
import com.pookie.octfis.data.remote.CallStateHolder
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CallMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "octfis_call_monitor"
        const val NOTIF_ID   = 1001
        const val TAG        = "CallMonitorService"

        fun start(context: Context) {
            val intent = Intent(context, CallMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceJob   = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var windowManager: WindowManager
    private var overlayView: android.view.View? = null

    private var previousState  = TelephonyManager.CALL_STATE_IDLE
    private var incomingNumber = ""

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        windowManager    = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        registerPhoneListener()
        // Pre-warm contact cache so number matching works even if app was never opened
        ensureContactCacheLoaded()
        Log.d(TAG, "Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // RESTART_STICKY re-creates the service if killed; re-delivers last intent
        Log.d(TAG, "onStartCommand called")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        unregisterPhoneListener()
        removeOverlay()
        Log.d(TAG, "Service destroyed")
    }

    // ── Cache warm-up (Step 2 — retry-safe) ───────────────────────────────────

    private fun ensureContactCacheLoaded() {
        if (ContactRepository.cache.isNotEmpty()) return
        serviceScope.launch {
            var attempt = 0
            val maxAttempts = 3
            val retryDelayMs = 5_000L

            while (attempt < maxAttempts) {
                attempt++
                try {
                    // Guard: check token is present before hitting network
                    val tokenStore = ZohoServiceLocator.getTokenStore()
                    val hasToken = tokenStore.getAccessToken()?.isNotBlank() == true
                            || tokenStore.getRefreshToken()?.isNotBlank() == true

                    if (!hasToken) {
                        Log.w(TAG, "Cache warm-up attempt $attempt/$maxAttempts: no token yet, retrying in ${retryDelayMs/1000}s")
                        delay(retryDelayMs)
                        continue
                    }

                    val repo = ContactRepository(ZohoServiceLocator.getApiService())
                    var page = 1
                    var hasMore = true

                    while (hasMore) {
                        val result = repo.getContacts(page)
                        hasMore = result.getOrNull()?.second == true
                        page++
                    }

                    Log.d(TAG, "Cache loaded on attempt $attempt: ${ContactRepository.cache.size} contacts")
                    return@launch   // success — exit the retry loop

                } catch (e: Exception) {
                    Log.w(TAG, "Cache warm-up attempt $attempt/$maxAttempts failed: ${e.message}")
                    if (attempt < maxAttempts) delay(retryDelayMs)
                }
            }

            Log.w(TAG, "Cache warm-up gave up after $maxAttempts attempts (user may not be logged in)")
        }
    }

    // ── Phone state listener ───────────────────────────────────────────────────

    private var telephonyCallback: Any? = null
    @Suppress("DEPRECATION")
    private var legacyListener: PhoneStateListener? = null

    @SuppressLint("MissingPermission")
    private fun registerPhoneListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cb = object : TelephonyCallback(),
                TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    // API 31+: no number supplied — resolved later via CallLog
                    handleStateChange(state, "")
                }
            }
            telephonyCallback = cb
            telephonyManager.registerTelephonyCallback(mainExecutor, cb)
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    handleStateChange(state, phoneNumber ?: "")
                }
            }
            legacyListener = listener
            @Suppress("DEPRECATION")
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    @Suppress("DEPRECATION")
    private fun unregisterPhoneListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (telephonyCallback as? TelephonyCallback)?.let {
                telephonyManager.unregisterTelephonyCallback(it)
            }
        } else {
            legacyListener?.let {
                telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
            }
        }
    }

    // ── State machine ──────────────────────────────────────────────────────────

    private fun handleStateChange(state: Int, rawNumber: String) {
        Log.d(TAG, "State: $previousState → $state  number='$rawNumber'")

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                incomingNumber = rawNumber
                CallStateHolder.callDirection = "Inbound"
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (previousState == TelephonyManager.CALL_STATE_IDLE) {
                    CallStateHolder.callDirection = "Outbound"
                    incomingNumber = rawNumber
                }
                CallStateHolder.callStartMillis = System.currentTimeMillis()
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                if (previousState == TelephonyManager.CALL_STATE_OFFHOOK ||
                    previousState == TelephonyManager.CALL_STATE_RINGING
                ) {
                    CallStateHolder.callEndMillis = System.currentTimeMillis()

                    // On API 31+ the number isn't delivered via TelephonyCallback,
                    // so we read it from CallLog a moment after the call ends.
                    val knownNumber = incomingNumber.ifBlank { CallStateHolder.phoneNumber }
                    if (knownNumber.isNotBlank()) {
                        onCallEnded(knownNumber)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Give CallLog a second to write the record, then look it up
                        serviceScope.launch {
                            delay(2_500)  // give CallLog more time to write
                            val number = readLastCallLogNumber()
                            onCallEnded(number)
                        }
                    } else {
                        Log.d(TAG, "Call ended but number is blank — skipping")
                    }

                    incomingNumber = ""
                }
            }
        }

        previousState = state
    }

    // ── Step 6 — Robust onCallEnded with fallbacks ─────────────────────────────

    private fun onCallEnded(number: String) {
        if (number.isBlank()) {
            Log.d(TAG, "onCallEnded: number is blank after all lookups")
            // Fallback A: if a contact was pre-populated (e.g. call from ContactDetailScreen)
            if (CallStateHolder.contactZohoId.isNotBlank()) {
                Log.d(TAG, "Fallback A: using pre-set contactZohoId=${CallStateHolder.contactZohoId}")
                CallStateHolder.isFromService = true
                android.os.Handler(android.os.Looper.getMainLooper()).post { showOverlay() }
                return
            }
            // Fallback B: open PostCallLogActivity in "unknown number" mode
            // The user can still pick a contact manually inside that screen
            Log.d(TAG, "Fallback B: launching PostCallLogActivity with unknown number")
            CallStateHolder.contactName = "Unknown"
            CallStateHolder.phoneNumber = ""
            CallStateHolder.isFromService = true
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                openPostCallActivity()
            }
            return
        }

        val matchedContact = findContactByNumber(number)
        if (matchedContact != null) {
            Log.d(TAG, "Matched: ${matchedContact.fullName} (${matchedContact.zohoId})")
            CallStateHolder.contactZohoId = matchedContact.zohoId
            CallStateHolder.contactName   = matchedContact.fullName
            CallStateHolder.phoneNumber   = number
            CallStateHolder.isFromService = true
            // Always show overlay for service-detected calls
            android.os.Handler(android.os.Looper.getMainLooper()).post { showOverlay() }
        } else {
            Log.d(TAG, "No Zoho contact found for number='$number'")
            // Optional: still show overlay for manual logging with unmatched numbers
            // Uncomment the block below if you want to log calls to unknown contacts too:
            //
            // CallStateHolder.contactName = number   // show the raw number as name
            // CallStateHolder.phoneNumber = number
            // CallStateHolder.contactZohoId = ""     // no Who_Id — will log without contact link
            // CallStateHolder.isFromService = true
            // android.os.Handler(android.os.Looper.getMainLooper()).post { showOverlay() }
        }
    }

    // ── CallLog lookup (API 31+ fallback) ─────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun readLastCallLogNumber(): String {
        return try {
            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE),
                null, null,
                "${CallLog.Calls.DATE} DESC"
            ) ?: return ""
            cursor.use {
                if (it.moveToFirst()) {
                    val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)) ?: ""
                    val date   = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    // Only trust if the call ended in the last 30 seconds
                    if (System.currentTimeMillis() - date < 60_000L) number else ""
                } else ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "CallLog read failed: ${e.message}")
            ""
        }
    }

    // ── Step 1 — Improved contact matching with number normalization ───────────

    private fun findContactByNumber(rawNumber: String): com.pookie.octfis.data.model.Contact? {
        if (rawNumber.isBlank()) return null

        val normalized = normalizePhone(rawNumber)
        if (normalized.length < 7) return null   // too short to be meaningful

        return ContactRepository.cache.firstOrNull { contact ->
            listOf(contact.mobile, contact.phone).any { stored ->
                if (stored.isBlank()) return@any false
                val storedNorm = normalizePhone(stored)
                // Compare last 10 digits — works regardless of country-code format
                storedNorm.takeLast(10) == normalized.takeLast(10)
            }
        }
    }

    /**
     * Strips all non-digit characters, then removes leading country-code prefixes
     * so that +91XXXXXXXXXX, 0091XXXXXXXXXX, 0XXXXXXXXXX and XXXXXXXXXX all
     * reduce to the same 10-digit string (for Indian numbers; logic is generic).
     */
    private fun normalizePhone(number: String): String {
        // Remove everything that is not a digit
        var digits = number.replace(Regex("[^0-9]"), "")

        // Strip common international prefixes: 00<cc> or leading 0
        digits = when {
            digits.startsWith("0091") && digits.length > 12 -> digits.drop(4)  // 0091 + 10 digits
            digits.startsWith("91")   && digits.length == 12 -> digits.drop(2) // 91 + 10 digits
            digits.startsWith("0")    && digits.length == 11 -> digits.drop(1) // 0 + 10 digits
            else -> digits
        }
        return digits
    }

    // ── Step 5 — Robust overlay show/hide for MIUI + Android 12/13/14 ─────────

    private fun canShowOverlay(): Boolean {
        // Standard check
        if (android.provider.Settings.canDrawOverlays(this)) return true

        // MIUI workaround: try to detect MIUI permission via AppOpsManager
        return try {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val op = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                android.app.AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW
            } else {
                @Suppress("DEPRECATION")
                "android:system_alert_window"
            }
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(op, android.os.Process.myUid(), packageName)
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(op, android.os.Process.myUid(), packageName)
            }
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.w(TAG, "AppOps overlay check failed: ${e.message}")
            false
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return

        if (!canShowOverlay()) {
            Log.w(TAG, "Overlay permission not granted — cannot show overlay")
            // Fallback: launch PostCallLogActivity directly without overlay
            openPostCallActivity()
            return
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        // FLAG_NOT_TOUCH_MODAL ensures touches outside the overlay pass through
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 24
            y = 120
            // On Android 12+ set softInputMode to avoid interaction issues
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                softInputMode = android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
            }
        }

        val view = LayoutInflater.from(this).inflate(R.layout.overlay_call_log, null)
        view.findViewById<ImageButton>(R.id.btnOverlay).setOnClickListener {
            removeOverlay()
            openPostCallActivity()
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
            Log.d(TAG, "Overlay shown")
        } catch (e: Exception) {
            Log.e(TAG, "addView failed (${e.message}) — launching PostCallLogActivity directly")
            // Last-resort fallback: skip overlay, go straight to logging dialog
            openPostCallActivity()
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            runCatching { windowManager.removeView(it) }
            overlayView = null
            Log.d(TAG, "Overlay removed")
        }
    }

    private fun openPostCallActivity() {
        val intent = Intent(this, PostCallLogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    // ── Notification ───────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Monitor",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Monitors calls to log them to Zoho CRM"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Octfis CRM")
            .setContentText("Monitoring calls for Zoho CRM logging")
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()
}