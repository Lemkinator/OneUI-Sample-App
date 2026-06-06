package de.lemke.oneuisample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.lemke.oneuisample.data.initUserSettingsAndSetDarkMode

/**
 * Main entry point into the application process.
 * Registered in the AndroidManifest.xml file.
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initUserSettingsAndSetDarkMode()
    }
}
