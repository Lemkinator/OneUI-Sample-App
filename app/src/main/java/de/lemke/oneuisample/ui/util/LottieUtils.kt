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
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import de.lemke.oneuisample.R.color.primary_color_themed
import java.util.WeakHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Default delay before a Lottie animation starts playing, to avoid flicker on fast data loads. */
val DEFAULT_LOTTIE_DELAY = 400.milliseconds

private val pendingPlayJobs = WeakHashMap<LottieAnimationView, Job>()

/**
 * Optionally sets [animation], applies the theme color, and plays.
 * If [cancelFirst] is true (default), the current animation is canceled and reset to frame 0 first.
 * When [delay] is [Duration.ZERO] (default), plays immediately on the calling thread.
 * Otherwise requires an attached view tree lifecycle owner; does nothing if none is found.
 */
fun LottieAnimationView.play(
    animation: String? = null,
    cancelFirst: Boolean = true,
    delay: Duration = Duration.ZERO,
) {
    pendingPlayJobs.remove(this)?.cancel()
    if (cancelFirst) {
        cancelAnimation()
        progress = 0f
    }
    animation?.let { setAnimation(it) }
    addValueCallback(
        KeyPath("**"),
        LottieProperty.COLOR_FILTER,
        LottieValueCallback(SimpleColorFilter(context.getColor(primary_color_themed))),
    )
    if (delay == Duration.ZERO) {
        playAnimation()
    } else {
        findViewTreeLifecycleOwner()?.let { owner ->
            val weakView = java.lang.ref.WeakReference(this)
            pendingPlayJobs[this] =
                owner.lifecycleScope.launch {
                    delay(delay)
                    weakView.get()?.playAnimation()
                }
        }
    }
}
