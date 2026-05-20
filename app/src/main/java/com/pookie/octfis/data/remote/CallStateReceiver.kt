package com.pookie.octfis.data.remote

/**
 * Shared call-session state.
 *
 * The BroadcastReceiver approach (PHONE_STATE) is dead on Android 9+
 * for third-party apps — the system never delivers the broadcast.
 *
 * Instead we use a simple before/after timestamp strategy:
 *  1. Record [callInitiatedAtMillis] just before ACTION_CALL fires.
 *  2. On ContactDetailScreen onResume, if [callInitiatedAtMillis] > 0
 *     we know a call was attempted and show the post-call dialog.
 *  3. [callStartMillis] / [callEndMillis] are set to the bracket
 *     (initiatedAt … resumeAt) as best-effort duration since we
 *     cannot observe real off-hook/idle transitions.
 */
object CallStateHolder {

    /** Zoho record ID of the contact being called. */
    var contactZohoId: String = ""

    /** Display name of the contact being called. */
    var contactName: String = ""

    /**
     * Wall-clock ms recorded immediately before startActivity(ACTION_CALL).
     * Non-zero means a call was initiated this session.
     */
    var callInitiatedAtMillis: Long = 0L

    /**
     * Set to [callInitiatedAtMillis] when we have no better signal;
     * gives the ViewModel a non-zero start to format the timestamp.
     */
    var callStartMillis: Long = 0L

    /**
     * Set to System.currentTimeMillis() in onResume after a call.
     * Non-zero + callInitiatedAtMillis > 0 → call cycle complete.
     */
    var callEndMillis: Long = 0L

    /** True while the dialer is in the foreground (between initiate and resume). */
    var isCallActive: Boolean = false

    /** Reset all state after the dialog is dismissed or saved. */
    fun reset() {
        contactZohoId        = ""
        contactName          = ""
        callInitiatedAtMillis = 0L
        callStartMillis      = 0L
        callEndMillis        = 0L
        isCallActive         = false
    }
}