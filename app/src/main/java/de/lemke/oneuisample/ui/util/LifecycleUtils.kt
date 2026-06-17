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

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Launches [block] in the lifecycle scope, repeating it whenever the lifecycle reaches [minActiveState]. */
inline fun AppCompatActivity.launchAndRepeatWithLifecycle(
    minActiveState: State = STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit,
) {
    lifecycleScope.launch { lifecycle.repeatOnLifecycle(minActiveState) { block() } }
}

/** Launches [block] in the view lifecycle scope, repeating it whenever the view lifecycle reaches [minActiveState]. */
inline fun Fragment.launchAndRepeatWithViewLifecycle(
    minActiveState: State = STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch { viewLifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) { block() } }
}

/** Collects [flow] emissions and delivers each value to [onEach] while the activity is at least [minActiveState]. */
inline fun <T> AppCompatActivity.collectState(
    flow: StateFlow<T>,
    minActiveState: State = STARTED,
    crossinline onEach: (T) -> Unit,
) = launchAndRepeatWithLifecycle(minActiveState) {
    flow.collect { onEach(it) }
}

/** Collects [flow] emissions and delivers each value to [onEach] while the fragment view is at least [minActiveState]. */
inline fun <T> Fragment.collectState(
    flow: StateFlow<T>,
    minActiveState: State = STARTED,
    crossinline onEach: (T) -> Unit,
) = launchAndRepeatWithViewLifecycle(minActiveState) {
    flow.collect { onEach(it) }
}

/** Collects [flow] events and delivers each to [onEach] while the activity is at least [minActiveState]. */
inline fun <T> AppCompatActivity.collectEvents(
    flow: Flow<T>,
    minActiveState: State = STARTED,
    crossinline onEach: (T) -> Unit,
) = launchAndRepeatWithLifecycle(minActiveState) {
    flow.collect { onEach(it) }
}

/** Collects [flow] events and delivers each to [onEach] while the fragment view is at least [minActiveState]. */
inline fun <T> Fragment.collectEvents(
    flow: Flow<T>,
    minActiveState: State = STARTED,
    crossinline onEach: (T) -> Unit,
) = launchAndRepeatWithViewLifecycle(minActiveState) {
    flow.collect { onEach(it) }
}

/**
 * Returns a [StateFlow] backed by this flow, using [SharingStarted.WhileSubscribed] with a
 * 5-second timeout. Intended for ViewModel state derived from a repository flow.
 */
fun <T> Flow<T>.stateInViewModel(
    scope: CoroutineScope,
    initialValue: T,
): StateFlow<T> =
    stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialValue,
    )
