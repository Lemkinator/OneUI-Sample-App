/*
 * Copyright 2022-2026 Leonard Lemke
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

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import de.lemke.oneuisample.R
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Applies the app theme's primary color to all Lottie layers. */
fun LottieAnimationView.applyThemeColor(context: Context) {
    addValueCallback(KeyPath("**"), COLOR_FILTER, LottieValueCallback(SimpleColorFilter(context.getColor(R.color.primary_color_themed))))
}

/** Applies the theme color and plays the animation immediately. */
fun LottieAnimationView.playWithThemeColor(context: Context) {
    applyThemeColor(context)
    playAnimation()
}

/** Cancels the current animation, resets to frame 0, applies the theme color, and plays after [delayMs]. */
fun LottieAnimationView.resetAndPlay(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    delayMs: Long,
) {
    cancelAnimation()
    progress = 0f
    applyThemeColor(context)
    lifecycleOwner.lifecycleScope.launch {
        delay(delayMs.milliseconds)
        playAnimation()
    }
}
