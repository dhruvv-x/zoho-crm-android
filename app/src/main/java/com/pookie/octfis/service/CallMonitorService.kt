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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        unregisterPhoneListener()
        removeOverlay()
        Log.d(TAG, "Service destroyed")
    }

    // ── Cache warm-up ──────────────────────────────────────────────────────────

    /**
     * If the cache is empty (e.g. after reboot), fetch contacts from Zoho so
     * number-matching works for calls that happen before the app is opened.
     */
    private fun ensureContactCacheLoaded() {
        if (ContactRepository.cache.isNotEmpty()) return
        serviceScope.launch {
            try {
                val repo = ContactRepository(ZohoServiceLocator.getApiService())
                var page = 1
                var hasMore = true
                while (hasMore) {
                    val result = repo.getContacts(page)
                    hasMore = result.getOrNull()?.second == true
                    page++
                }
                Log.d(TAG, "Cache loaded: ${ContactRepository.cache.size} contacts")
            } catch (e: Exception) {
                Log.w(TAG, "Cache warm-up failed (user may not be logged in yet): ${e.message}")
            }
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
                            delay(1_500)
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

    private fun onCallEnded(number: String) {
        if (number.isBlank()) {
            Log.d(TAG, "onCallEnded: number still blank after CallLog lookup")
            return
        }

        val matchedContact = findContactByNumber(number)
        if (matchedContact != null) {
            Log.d(TAG, "Matched: ${matchedContact.fullName} (${matchedContact.zohoId})")
            CallStateHolder.contactZohoId = matchedContact.zohoId
            CallStateHolder.contactName   = matchedContact.fullName
            CallStateHolder.phoneNumber   = number
            CallStateHolder.isFromService = true
            if (!CallStateHolder.isCallActive) {
                // Post to main thread — WindowManager requires it
                android.os.Handler(android.os.Looper.getMainLooper()).post { showOverlay() }
            }
        } else {
            Log.d(TAG, "No Zoho contact found for number='$number'")
        }
    }

    // ── CallLog lookup (API 31+ fallback) ─────────────────────────────────────

    /**
     * Reads the most recent entry from the system CallLog.
     * Only called on Android 12+ where TelephonyCallback gives no number.
     * Requires READ_CALL_LOG permission.
     */
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
                    if (System.currentTimeMillis() - date < 30_000L) number else ""
                } else ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "CallLog read failed: ${e.message}")
            ""
        }
    }

    // ── Contact matching ───────────────────────────────────────────────────────

    private fun findContactByNumber(number: String): com.pookie.octfis.data.model.Contact? {
        if (number.isBlank()) return null
        val normalized = number.replace(Regex("[\\s\\-().]+"), "")
        return ContactRepository.cache.firstOrNull { contact ->
            listOf(contact.mobile, contact.phone).any { stored ->
                if (stored.isBlank()) return@any false
                val storedNorm = stored.replace(Regex("[\\s\\-().]+"), "")
                storedNorm.takeLast(10) == normalized.takeLast(10)
            }
        }
    }

    // ── Floating overlay ───────────────────────────────────────────────────────

    private fun showOverlay() {
        if (overlayView != null) return

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 24
            y = 120
        }

        val view = LayoutInflater.from(this).inflate(R.layout.overlay_call_log, null)
        view.findViewById<ImageButton>(R.id.btnOverlay).setOnClickListener {
            removeOverlay()
            openPostCallActivity()
        }

        windowManager.addView(view, params)
        overlayView = view
        Log.d(TAG, "Overlay shown")
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