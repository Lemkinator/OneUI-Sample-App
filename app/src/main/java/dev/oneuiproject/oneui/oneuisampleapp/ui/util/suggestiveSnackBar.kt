@file:Suppress("unused")

package dev.oneuiproject.oneui.oneuisampleapp.ui.util

import android.R.id.content
import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SESL_SNACKBAR_TYPE_SUGGESTION


inline fun Fragment.suggestiveSnackBar(
    @StringRes msg: Int,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = requireActivity().suggestiveSnackBar(msg, view, duration, actionText, action)

inline fun Fragment.suggestiveSnackBar(
    msg: String,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = requireActivity().suggestiveSnackBar(msg, view, duration, actionText, action)

inline fun Activity.suggestiveSnackBar(
    @StringRes msg: Int,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = suggestiveSnackBar(getString(msg), view, duration, actionText, action)

inline fun Activity.suggestiveSnackBar(
    msg: String,
    view: View? = null,
    duration: Int? = null,
    actionText: String? = null,
    crossinline action: (Snackbar.() -> Unit) = { dismiss() },
) = Snackbar.make(view ?: findViewById(content), msg, duration ?: LENGTH_SHORT, SESL_SNACKBAR_TYPE_SUGGESTION).apply {
    actionText?.let { setAction(it) { action() } }
    show()
}
