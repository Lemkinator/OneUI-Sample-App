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

import android.R
import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SESL_SNACKBAR_TYPE_SUGGESTION
import de.lemke.oneuisample.NoCoverage

/** Shows a OneUI suggestive snackbar with [msg] string resource and an optional action. */
inline fun Fragment.suggestiveSnackBar(
    @StringRes msg: Int,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = requireActivity().suggestiveSnackBar(msg, view, duration, actionText, action)

/** Shows a OneUI suggestive snackbar with [msg] and an optional action. */
inline fun Fragment.suggestiveSnackBar(
    msg: String,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = requireActivity().suggestiveSnackBar(msg, view, duration, actionText, action)

/** Shows a OneUI suggestive snackbar with [msg] string resource and an optional action. */
inline fun Activity.suggestiveSnackBar(
    @StringRes msg: Int,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = suggestiveSnackBar(getString(msg), view, duration, actionText, action)

/** Shows a OneUI suggestive snackbar with [msg] and an optional action. */
@NoCoverage
inline fun Activity.suggestiveSnackBar(
    msg: String,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = Snackbar.make(view ?: findViewById(R.id.content), msg, duration ?: LENGTH_SHORT, SESL_SNACKBAR_TYPE_SUGGESTION).apply {
    actionText?.let { setAction(it) { action() } }
    show()
}
