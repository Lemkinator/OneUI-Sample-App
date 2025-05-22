@file:Suppress("unused")

package dev.oneuiproject.oneui.oneuisampleapp.ui.util

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(msg: String) = requireContext().toast(msg)
