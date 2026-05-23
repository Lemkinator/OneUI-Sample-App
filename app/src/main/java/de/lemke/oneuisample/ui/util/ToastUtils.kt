@file:Suppress("unused")

package de.lemke.oneuisample.ui.util

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.toast(msg: String) = context?.toast(msg)

fun Context.toast(msg: String) = Toast.makeText(this, msg, LENGTH_SHORT).show()

fun Fragment.toast(
    @StringRes stringResId: Int,
) = context?.toast(stringResId)

fun Context.toast(
    @StringRes stringResId: Int,
) = Toast.makeText(this, stringResId, LENGTH_SHORT).show()
