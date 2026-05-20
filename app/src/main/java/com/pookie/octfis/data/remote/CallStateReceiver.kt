package com.pookie.octfis.data.remote

/**
 * Shared call-session state used by both:
 *  - ContactDetailScreen (in-app calls via onResume)
 *  - CallMonitorService  (background calls via TelephonyCallback)
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
    /** Set just before ACTION_CALL fires (in-app path) */
    var callInitiatedAtMillis : Long = 0L

    /** Best-effort call start (OFFHOOK time from service, or initiatedAt) */
    var callStartMillis : Long = 0L

    /** Set when IDLE is detected (service) or onResume fires (in-app) */
    var callEndMillis   : Long = 0L

    /** True while dialer is in foreground / call is active */
    var isCallActive    : Boolean = false

    // ── Source flag ───────────────────────────────────────────────────────────
    /** True when the call was detected by CallMonitorService (not in-app) */
    var isFromService   : Boolean = false

    /** Reset everything after dialog is dismissed or saved */
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