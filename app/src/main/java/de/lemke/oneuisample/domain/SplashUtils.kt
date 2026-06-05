package de.lemke.oneuisample.domain

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.View.ALPHA
import android.view.View.SCALE_X
import android.view.View.SCALE_Y
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.KeepOnScreenCondition
import androidx.lifecycle.lifecycleScope
import java.lang.System.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_ANIMATION_DURATION_MS = 400L
private const val SPLASH_SCALE_FACTOR = 1.2f

fun AppCompatActivity.configureSplashScreen(
    splashScreen: SplashScreen,
    root: View,
    condition: KeepOnScreenCondition? = null,
) {
    condition?.let { splashScreen.setKeepOnScreenCondition(it) }
    splashScreen.setOnExitAnimationListener { splash ->
        val splashAnimator: ObjectAnimator =
            ObjectAnimator.ofPropertyValuesHolder(
                splash.view,
                PropertyValuesHolder.ofFloat(ALPHA, 0f),
                PropertyValuesHolder.ofFloat(SCALE_X, SPLASH_SCALE_FACTOR),
                PropertyValuesHolder.ofFloat(SCALE_Y, SPLASH_SCALE_FACTOR),
            )
        splashAnimator.interpolator = AccelerateDecelerateInterpolator()
        splashAnimator.duration = SPLASH_ANIMATION_DURATION_MS
        splashAnimator.doOnEnd { splash.remove() }
        val contentAnimator: ObjectAnimator =
            ObjectAnimator.ofPropertyValuesHolder(
                root,
                PropertyValuesHolder.ofFloat(ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(SCALE_X, SPLASH_SCALE_FACTOR, 1f),
                PropertyValuesHolder.ofFloat(SCALE_Y, SPLASH_SCALE_FACTOR, 1f),
            )
        contentAnimator.interpolator = AccelerateDecelerateInterpolator()
        contentAnimator.duration = SPLASH_ANIMATION_DURATION_MS
        val remainingDuration =
            splash.iconAnimationDurationMillis - (currentTimeMillis() - splash.iconAnimationStartMillis).coerceAtLeast(0L)
        lifecycleScope.launch {
            delay(remainingDuration)
            splashAnimator.start()
            contentAnimator.start()
        }
    }
}
