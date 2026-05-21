package com.pookie.octfis

import android.app.Application
import android.content.SharedPreferences
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.service.CallMonitorService

class OctfisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ZohoServiceLocator.init(this)
        // Start service from Application so it survives activity restarts
        val prefs: SharedPreferences = getSharedPreferences("octfis_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("overlay_permission_asked", false)) {
            CallMonitorService.start(this)
        }
    }
}