package de.lemke.oneuisample.domain

import android.R.anim.fade_in
import android.R.anim.fade_out
import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE

/**
 * Applies a fade-in open transition for this activity.
 *
 * **Call in two places for full API coverage:**
 * - **Destination** `onCreate` (after `super.onCreate`) — required for API 34+;
 *   overridePendingTransition is a no-op there on pre-34.
 * - **Source** immediately after a bare `startActivity` (no finish) — required for pre-34.
 *   On API 34+, overrideActivityTransition works from both sides; if both source and destination
 *   call this, they agree on the same animations and the redundancy is harmless.
 *
 * When the source calls `startActivity` and then [finishWithFade], the source-side call here is
 * unnecessary — [finishWithFade] already applies the entering fade to the new activity on pre-34.
 */
fun Activity.overrideFadeOpenTransition() {
    if (SDK_INT >= UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, fade_in, fade_out)
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(fade_in, fade_out)
    }
}

/** Applies a fade-out close transition and finishes the activity (predictive-back aware). */
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
