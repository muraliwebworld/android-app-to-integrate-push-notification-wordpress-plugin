package com.sample.pnfpbandroid

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class with Hilt initialization and Timber setup
 * Hilt dependency injection framework initialization
 */
@HiltAndroidApp
class PushNotificationApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.i("Application initialized")
    }
}
