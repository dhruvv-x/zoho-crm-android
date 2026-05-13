package com.pookie.octfis

import android.app.Application
import com.pookie.octfis.data.remote.ZohoServiceLocator

class OctfisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ZohoServiceLocator.init(this)
    }
}