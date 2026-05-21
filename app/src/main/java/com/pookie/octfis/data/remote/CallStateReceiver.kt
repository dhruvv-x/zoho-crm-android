package com.pookie.octfis.data.remote

/**
 * Shared call-session state used by both:
 *  - ContactDetailScreen (in-app calls via launchCall())
 *  - CallMonitorService  (background calls via TelephonyCallback)
 *
 * ISSUE 3 FIX — timestamps ownership:
 *   callInitiatedAtMillis → set by ContactDetailScreen right before ACTION_CALL fires
 *   callStartMillis       → set by CALL_STATE_OFFHOOK in CallMonitorService (precise)
 *   callEndMillis         → set by CALL_STATE_IDLE   in CallMonitorService (precise)
 */
object CallStateHolder {

    // ── Contact info ──────────────────────────────────────────────────────────
    var contactZohoId : String = ""
    var contactName   : String = ""

    // ── Raw phone number dialed/received (set by CallMonitorService) ──────────
    var phoneNumber   : String = ""

    // ── Call direction: "Outbound" or "Inbound" ───────────────────────────────
    var callDirection : String = "Outbound"

    // ── Timestamps ────────────────────────────────────────────────────────────
    /** Set just before ACTION_CALL fires (in-app path). Fallback only. */
    var callInitiatedAtMillis : Long = 0L

    /** Set by CALL_STATE_OFFHOOK in CallMonitorService — the precise answer-time. */
    var callStartMillis : Long = 0L

    /** Set by CALL_STATE_IDLE in CallMonitorService — the precise hang-up time. */
    var callEndMillis   : Long = 0L

    /** True while the dialer is in foreground / call is active (in-app path). */
    var isCallActive    : Boolean = false

    // ── Source flag ───────────────────────────────────────────────────────────
    /** True when the call was detected/closed by CallMonitorService. */
    var isFromService   : Boolean = false

    /** Reset everything after the dialog is dismissed or saved. */
    fun reset() {
        contactZohoId         = ""
        contactName           = ""
        phoneNumber           = ""
        callDirection         = "Outbound"
        callInitiatedAtMillis = 0L
        callStartMillis       = 0L
        callEndMillis         = 0L
        isCallActive          = false
        isFromService         = false
    }
}