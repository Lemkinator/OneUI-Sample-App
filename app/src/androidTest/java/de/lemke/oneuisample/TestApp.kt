package de.lemke.oneuisample

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.testing.CustomTestApplication
import de.lemke.oneuisample.data.UserSettingsRepository

open class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        // Bypass OOBE: fresh SharedPreferences has lastVersionCode = -1 which triggers
        // onboardIfNeeded → finishWithFade, leaving MainActivity DESTROYED.
        getSharedPreferences(UserSettingsRepository.PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(UserSettingsRepository::lastVersionCode.name, Int.MAX_VALUE)
            .putInt(UserSettingsRepository::acceptedTosVersion.name, Int.MAX_VALUE)
            .apply()
    }
}

@Suppress("unused") // KSP generates TestApplication_Application from this annotation target
@CustomTestApplication(TestApp::class)
interface TestApplication
