package de.lemke.oneuisample

import android.content.SharedPreferences
import de.lemke.oneuisample.data.UserSettingsRepository

fun SharedPreferences.bypassOobe() {
    edit()
        .putInt(UserSettingsRepository::lastVersionCode.name, Int.MAX_VALUE)
        .putInt(UserSettingsRepository::acceptedTosVersion.name, Int.MAX_VALUE)
        .commit()
}
