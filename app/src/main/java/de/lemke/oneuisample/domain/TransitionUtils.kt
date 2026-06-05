package de.lemke.oneuisample.domain

import android.R.anim.fade_in
import android.R.anim.fade_out
import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE

fun Activity.overrideFadeOpenTransition() {
    if (SDK_INT >= UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, fade_in, fade_out)
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(fade_in, fade_out)
    }
}

fun Activity.finishWithFade() {
    if (SDK_INT >= UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, fade_in, fade_out)
        finishAfterTransition()
    } else {
        finishAfterTransition()
        @Suppress("DEPRECATION")
        overridePendingTransition(fade_in, fade_out)
    }
}
