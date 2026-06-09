package de.lemke.oneuisample

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.testing.CustomTestApplication

open class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        // Bypass OOBE: fresh SharedPreferences has lastVersionCode = -1 which triggers
        // onboardIfNeeded → finishWithFade, leaving MainActivity DESTROYED.
        getSharedPreferences("user_settings", Context.MODE_PRIVATE)
            .edit()
            .putInt("lastVersionCode", Int.MAX_VALUE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .apply()
    }
}

@Suppress("unused") // KSP generates TestApplication_Application from this annotation target
@CustomTestApplication(TestApp::class)
interface TestApplication
