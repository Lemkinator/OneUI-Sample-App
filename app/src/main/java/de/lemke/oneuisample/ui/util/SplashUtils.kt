/*
 * Copyright 2024-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui.util

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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_ANIMATION_DURATION_MS = 400L
private const val SPLASH_SCALE_FACTOR = 1.2f

/** Configures the splash screen to stay visible while [condition] holds, then animates it out while fading in [root]. */
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
            delay(remainingDuration.milliseconds)
            splashAnimator.start()
            contentAnimator.start()
        }
    }
}
