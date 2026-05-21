package com.pookie.octfis.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Issue 2 fix: restart CallMonitorService after device reboot (and MIUI quick-boot).
 *
 * Declared in AndroidManifest.xml with RECEIVE_BOOT_COMPLETED permission and
 * intent-filters for BOOT_COMPLETED + QUICKBOOT_POWERON.
 * The manifest already has the correct declaration — this was the missing implementation.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d("BootReceiver", "Boot completed — starting CallMonitorService")
            CallMonitorService.start(context)
        }
    }
}