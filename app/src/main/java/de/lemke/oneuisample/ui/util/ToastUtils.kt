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
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/** Shows a short toast with the given message. */
fun Fragment.toast(msg: String) = requireContext().toast(msg)

/** Shows a short toast with the given message. */
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

/** Shows a short toast with the given string resource. */
fun Fragment.toast(
    @StringRes stringResId: Int,
) = requireContext().toast(stringResId)

/** Shows a short toast with the given string resource. */
fun Context.toast(
    @StringRes stringResId: Int,
) = Toast.makeText(this, stringResId, Toast.LENGTH_SHORT).show()
