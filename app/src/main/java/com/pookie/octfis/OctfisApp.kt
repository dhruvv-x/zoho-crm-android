package com.pookie.octfis

import android.app.Application
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.service.CallMonitorService

class OctfisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ZohoServiceLocator.init(this)
        CallMonitorService.start(this)
    }
}