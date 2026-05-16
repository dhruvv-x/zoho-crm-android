package com.pookie.octfis

import android.app.Application
import com.pookie.octfis.data.remote.ZohoServiceLocator
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OctfisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ZohoServiceLocator.init(this) // keeping this until all ViewModels are migrated to Hilt
    }
}