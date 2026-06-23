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

import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import de.lemke.oneuisample.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DEFAULT_LOTTIE_DELAY = 400.milliseconds

/** Applies the app theme's primary color to all Lottie layers. */
fun LottieAnimationView.applyThemeColor() {
    addValueCallback(
        KeyPath("**"),
        COLOR_FILTER,
        LottieValueCallback(SimpleColorFilter(context.getColor(R.color.primary_color_themed))),
    )
}

/** Optionally switches to [animation], applies the theme color, and plays immediately. */
fun LottieAnimationView.playWithThemeColor(animation: String? = null) {
    animation?.let { setAnimation(it) }
    applyThemeColor()
    playAnimation()
}

/**
 * Optionally switches to [animation], applies the theme color, and plays after [delay].
 * If [cancelFirst] is true (default), the current animation is cancelled and reset to frame 0 first.
 * Requires an attached view tree lifecycle owner; does nothing if none is found.
 */
fun LottieAnimationView.resetAndPlay(
    animation: String? = null,
    cancelFirst: Boolean = true,
    delay: Duration = DEFAULT_LOTTIE_DELAY,
) {
    animation?.let { setAnimation(it) }
    if (cancelFirst) {
        cancelAnimation()
        progress = 0f
    }
    applyThemeColor()
    val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
    lifecycleOwner.lifecycleScope.launch {
        delay(delay)
        playAnimation()
    }
}
