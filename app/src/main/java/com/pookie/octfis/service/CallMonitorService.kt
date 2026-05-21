package com.pookie.octfis.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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
        const val CHANNEL_ID          = "octfis_call_monitor"
        const val POST_CALL_CHANNEL_ID = "octfis_post_call"
        const val NOTIF_ID            = 1001
        const val POST_CALL_NOTIF_ID  = 1002
        const val TAG                 = "CallMonitorService"

        fun start(context: Context) {
            // Do not start if READ_PHONE_STATE is not granted — would crash on registerTelephonyCallback
            val hasPhoneState = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPhoneState) return

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
        // Guard: only register listener if permission is granted
        val hasPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            registerPhoneListener()
        } else {
            // No permission — stop self gracefully instead of crashing
            stopSelf()
            return
        }
        // Pre-warm contact cache so number matching works even if app was never opened
        ensureContactCacheLoaded()
        Log.e(TAG, ">>> Service started and phone listener registered")
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

    // Issue 2 fix: restart the service if it is killed (e.g. swiped from recents)
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed — scheduling restart")
        val restart = Intent(applicationContext, CallMonitorService::class.java)
        val pendingIntent = android.app.PendingIntent.getService(
            applicationContext,
            1,
            restart,
            android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarm.set(
            android.app.AlarmManager.ELAPSED_REALTIME,
            android.os.SystemClock.elapsedRealtime() + 1_000L,
            pendingIntent
        )
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
        Log.e(TAG, ">>> STATE CHANGE: $previousState → $state  number='$rawNumber'")

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                incomingNumber = rawNumber
                CallStateHolder.callDirection = "Inbound"
                Log.e(TAG, ">>> RINGING: incomingNumber='$incomingNumber'")
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (previousState == TelephonyManager.CALL_STATE_IDLE) {
                    CallStateHolder.callDirection = "Outbound"
                    incomingNumber = rawNumber
                }
                CallStateHolder.callStartMillis = System.currentTimeMillis()
                Log.e(TAG, ">>> OFFHOOK: direction=${CallStateHolder.callDirection} number='$incomingNumber'")
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                Log.e(TAG, ">>> IDLE: previousState=$previousState isCallActive=${CallStateHolder.isCallActive}")
                if (previousState == TelephonyManager.CALL_STATE_OFFHOOK ||
                    previousState == TelephonyManager.CALL_STATE_RINGING
                ) {
                    CallStateHolder.callEndMillis = System.currentTimeMillis()

                    if (CallStateHolder.isCallActive) {
                        Log.e(TAG, ">>> IDLE: in-app call path → opening PostCallLogActivity")
                        CallStateHolder.isCallActive  = false
                        CallStateHolder.isFromService = true
                        incomingNumber = ""
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            openPostCallActivity()
                        }
                        previousState = state
                        return
                    }

                    val knownNumber = incomingNumber.ifBlank { CallStateHolder.phoneNumber }
                    Log.e(TAG, ">>> IDLE: knownNumber='$knownNumber' SDK=${Build.VERSION.SDK_INT}")
                    if (knownNumber.isNotBlank()) {
                        Log.e(TAG, ">>> IDLE: calling onCallEnded with knownNumber")
                        onCallEnded(knownNumber)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Log.e(TAG, ">>> IDLE: API31+ no number, waiting 2.5s then reading CallLog")
                        serviceScope.launch {
                            delay(2_500)
                            val number = readLastCallLogNumber()
                            Log.e(TAG, ">>> IDLE: CallLog returned number='$number'")
                            onCallEnded(number)
                        }
                    } else {
                        Log.e(TAG, ">>> IDLE: number is blank and API<31 — skipping")
                    }

                    incomingNumber = ""
                } else {
                    Log.e(TAG, ">>> IDLE: ignoring — previousState=$previousState (not OFFHOOK/RINGING)")
                }
            }
        }

        previousState = state
    }

    // ── Robust onCallEnded with fallbacks ─────────────────────────────────────

    private fun onCallEnded(number: String) {
        Log.e(TAG, ">>> onCallEnded: number='$number'")
        if (number.isBlank()) {
            Log.e(TAG, ">>> onCallEnded: number blank — contactZohoId='${CallStateHolder.contactZohoId}'")
            if (CallStateHolder.contactZohoId.isNotBlank()) {
                Log.e(TAG, ">>> onCallEnded: Fallback A → opening PostCallLogActivity")
                CallStateHolder.isFromService = true
                android.os.Handler(android.os.Looper.getMainLooper()).post { openPostCallActivity() }
                return
            }
            Log.e(TAG, ">>> onCallEnded: Fallback B → unknown number, opening PostCallLogActivity")
            CallStateHolder.contactName = "Unknown"
            CallStateHolder.phoneNumber = ""
            CallStateHolder.isFromService = true
            android.os.Handler(android.os.Looper.getMainLooper()).post { openPostCallActivity() }
            return
        }

        val matchedContact = findContactByNumber(number)
        Log.e(TAG, ">>> onCallEnded: matchedContact=${matchedContact?.fullName ?: "null"}")
        if (matchedContact != null) {
            CallStateHolder.contactZohoId = matchedContact.zohoId
            CallStateHolder.contactName   = matchedContact.fullName
            CallStateHolder.phoneNumber   = number
            CallStateHolder.isFromService = true
            Log.e(TAG, ">>> onCallEnded: matched → opening PostCallLogActivity")
            android.os.Handler(android.os.Looper.getMainLooper()).post { openPostCallActivity() }
        } else {
            Log.e(TAG, ">>> onCallEnded: no match → opening PostCallLogActivity with raw number")
            CallStateHolder.contactName   = number
            CallStateHolder.phoneNumber   = number
            CallStateHolder.contactZohoId = ""
            CallStateHolder.isFromService = true
            android.os.Handler(android.os.Looper.getMainLooper()).post { openPostCallActivity() }
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
                    // Only trust if the call ended in the last 60 seconds
                    if (System.currentTimeMillis() - date < 60_000L) number else ""
                } else ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "CallLog read failed: ${e.message}")
            ""
        }
    }

    // ── Contact matching with number normalization ─────────────────────────────

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
        var digits = number.replace(Regex("[^0-9]"), "")
        digits = when {
            digits.startsWith("0091") && digits.length > 12 -> digits.drop(4)
            digits.startsWith("91")   && digits.length == 12 -> digits.drop(2)
            digits.startsWith("0")    && digits.length == 11 -> digits.drop(1)
            else -> digits
        }
        return digits
    }

    // ── Robust overlay show/hide for MIUI + Android 12/13/14 ──────────────────

    private fun openPostCallActivity() {
        Log.e(TAG, ">>> openPostCallActivity: launching PostCallLogActivity")
        if (!android.provider.Settings.canDrawOverlays(this)) {
            Log.e(TAG, ">>> No overlay permission — falling back to Activity")
            val intent = Intent(this, PostCallLogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            startActivity(intent)
            return
        }
        showDialogOverlay()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun showDialogOverlay() {
        if (overlayView != null) return

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
        }

        val view = LayoutInflater.from(this).inflate(R.layout.overlay_call_log, null)

        // Populate fields
        val dir = if (CallStateHolder.callDirection == "Inbound") "Incoming call from" else "Outgoing call to"
        view.findViewById<android.widget.TextView>(R.id.tvContact).text =
            "$dir ${CallStateHolder.contactName}"

        val start = CallStateHolder.callStartMillis.takeIf { it > 0L }
            ?: CallStateHolder.callInitiatedAtMillis.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val end = CallStateHolder.callEndMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        val secs = ((end - start) / 1000).coerceAtLeast(0)
        view.findViewById<android.widget.TextView>(R.id.tvDuration).text =
            "Duration: %02d:%02d".format(secs / 60, secs % 60)

        val etDesc = view.findViewById<android.widget.EditText>(R.id.etDescription)

        view.findViewById<android.widget.Button>(R.id.btnSkip).setOnClickListener {
            removeOverlay()
            CallStateHolder.reset()
        }

        view.findViewById<android.widget.Button>(R.id.btnSave).setOnClickListener {
            val description = etDesc.text.toString()
            val startMillis = CallStateHolder.callStartMillis.takeIf { it > 0L }
                ?: CallStateHolder.callInitiatedAtMillis.takeIf { it > 0L }
                ?: System.currentTimeMillis()
            val endMillis = CallStateHolder.callEndMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
            val durationSeconds = ((endMillis - startMillis) / 1000).coerceAtLeast(0)
            val durationStr = "%02d:%02d".format(durationSeconds / 60, durationSeconds % 60)
            val startTimeStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault())
                .format(java.util.Date(startMillis))
            val subject = if (CallStateHolder.callDirection == "Inbound")
                "Incoming call from ${CallStateHolder.contactName}"
            else
                "Outgoing call to ${CallStateHolder.contactName}"

            serviceScope.launch {
                try {
                    val repo = com.pookie.octfis.data.repository.CallRepository(
                        ZohoServiceLocator.getApiService()
                    )
                    repo.createCall(
                        subject       = subject,
                        callStartTime = startTimeStr,
                        duration      = durationStr,
                        callType      = CallStateHolder.callDirection,
                        status        = "Completed",
                        description   = description,
                        ownerId       = "",
                        whoId         = CallStateHolder.contactZohoId,
                    )
                    Log.e(TAG, ">>> Call logged successfully")
                } catch (e: Exception) {
                    Log.e(TAG, ">>> Failed to log call: ${e.message}")
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    removeOverlay()
                    CallStateHolder.reset()
                }
            }
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
            Log.e(TAG, ">>> Overlay dialog shown")
        } catch (e: Exception) {
            Log.e(TAG, ">>> addView failed: ${e.message}")
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            runCatching { windowManager.removeView(it) }
            overlayView = null
        }
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