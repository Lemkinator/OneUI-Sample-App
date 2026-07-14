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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.EXTRA_TITLE
import android.util.Log
import androidx.fragment.app.Fragment
import de.lemke.oneuisample.R

private const val MIME_TYPE_TEXT = "text/plain"
private const val TAG = "ShareUtils"

/** Shares [text] via the system share sheet with an optional chooser [title]. */
fun Fragment.shareText(
    text: String,
    title: String? = null,
): Boolean = requireContext().shareText(text, title)

/** Shares [text] via the system share sheet with an optional chooser [title]. */
fun Context.shareText(
    text: String,
    title: String? = null,
): Boolean {
    Intent().apply {
        action = ACTION_SEND
        putExtra(EXTRA_TEXT, text)
        putExtra(EXTRA_TITLE, title)
        type = MIME_TYPE_TEXT
        return safeStartActivity(Intent.createChooser(this, title))
    }
}

private fun Context.safeStartActivity(intent: Intent): Boolean {
    try {
        startActivity(intent)
        return true
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "Failed to start activity", e)
        toast(R.string.error_share_content_not_supported_on_device)
        return false
    }
}
