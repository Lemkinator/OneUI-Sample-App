package de.lemke.oneuisample

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import dagger.hilt.android.HiltAndroidApp
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var userSettings: UserSettingsRepository

    override fun onCreate() {
        super.onCreate()
        when {
            userSettings.autoDarkMode -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            userSettings.darkMode -> setDefaultNightMode(MODE_NIGHT_YES)
            else -> setDefaultNightMode(MODE_NIGHT_NO)
        }
    }
}
