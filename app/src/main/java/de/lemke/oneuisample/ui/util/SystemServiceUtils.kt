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
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import de.lemke.oneuisample.NoCoverage

/**
 * Looks up [serviceClass] via [ContextCompat.getSystemService] and, if present, runs [action] on it.
 * The null case (service unregistered for this Context) can't be produced under Robolectric for any
 * real platform service, so it stays untested here deliberately.
 */
@NoCoverage
fun <T, R> Context.withSystemService(
    serviceClass: Class<T>,
    action: (T) -> R,
): R? = ContextCompat.getSystemService(this, serviceClass)?.let(action)

/** Shows the soft keyboard for this view. Focus is requested by `ToolbarLayout.startSearchMode()`'s `isIconified = false`. */
@NoCoverage
fun View.showSoftInput(flags: Int = 0) {
    context.withSystemService(InputMethodManager::class.java) { it.showSoftInput(this, flags) }
}
