package com.pookie.octfis.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Restarts CallMonitorService after device reboot.
 * Only starts the service if the user has previously granted permissions
 * (we check via the same SharedPreferences flag MainActivity uses).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val prefs = context.getSharedPreferences("octfis_prefs", Context.MODE_PRIVATE)
        val overlayAsked = prefs.getBoolean("overlay_permission_asked", false)

        if (overlayAsked) {
            Log.d("BootReceiver", "Boot complete — starting CallMonitorService")
            CallMonitorService.start(context)
        } else {
            Log.d("BootReceiver", "Boot complete — skipping service start (user hasn't onboarded)")
        }
    }
}