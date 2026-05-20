package com.pookie.octfis.data.remote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

object CallStateHolder {
    var contactZohoId   : String  = ""
    var contactName     : String  = ""
    var callStartMillis : Long    = 0L
    var callEndMillis   : Long    = 0L
    var isCallActive    : Boolean = false
    var onCallEnded     : (() -> Unit)? = null
}

class CallStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

        android.util.Log.d("CALL_RECEIVER", "State received = $state")

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                CallStateHolder.callEndMillis = 0L
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                CallStateHolder.callEndMillis   = 0L
                CallStateHolder.callStartMillis = System.currentTimeMillis()
                CallStateHolder.isCallActive    = true
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (CallStateHolder.isCallActive) {
                    CallStateHolder.callEndMillis = System.currentTimeMillis()
                    CallStateHolder.isCallActive  = false
                    CallStateHolder.onCallEnded?.invoke()
                }
            }
        }
    }
}