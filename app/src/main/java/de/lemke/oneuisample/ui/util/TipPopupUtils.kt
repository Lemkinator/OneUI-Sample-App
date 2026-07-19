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

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.lemke.oneuisample.NoCoverage
import dev.oneuiproject.oneui.widget.TipPopup
import dev.oneuiproject.oneui.widget.TipPopup.Direction
import dev.oneuiproject.oneui.widget.TipPopup.Mode
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Shows a one-time [TipPopup] anchored to [getAnchor]'s view, auto-dismissing once the fragment's
 * view lifecycle drops below RESUMED. No-op if the fragment is not resumed or [getAnchor] returns null
 * (e.g., the anchor view isn't laid out yet) when the delay elapses.
 */
@NoCoverage
inline fun Fragment.showTipPopup(
    message: String,
    mode: Mode = Mode.TRANSLUCENT,
    direction: Direction = Direction.DEFAULT,
    delay: Duration = Duration.ZERO,
    expanded: Boolean = false,
    dismissOnPaused: Boolean = true,
    crossinline getAnchor: () -> View?,
    crossinline onCreate: TipPopup.() -> Unit = {},
) {
    viewLifecycleOwner.lifecycleScope.launch {
        delay(delay)
        if (!isActive || !isResumed) return@launch
        val anchor = getAnchor() ?: return@launch
        TipPopup(anchor, mode).apply {
            setMessage(message)
            onCreate()
            setExpanded(expanded)
            if (dismissOnPaused) {
                viewLifecycleOwner.lifecycle.addObserver(
                    object : DefaultLifecycleObserver {
                        override fun onPause(owner: LifecycleOwner) {
                            dismiss(true)
                            owner.lifecycle.removeObserver(this)
                        }
                    },
                )
            }
            show(direction)
        }
    }
}
