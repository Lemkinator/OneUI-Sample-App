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
