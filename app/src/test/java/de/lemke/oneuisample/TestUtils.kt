package de.lemke.oneuisample

import android.content.SharedPreferences

fun SharedPreferences.bypassOobe() {
    edit()
        .putInt("lastVersionCode", Int.MAX_VALUE)
        .putInt("acceptedTosVersion", Int.MAX_VALUE)
        .commit()
}
