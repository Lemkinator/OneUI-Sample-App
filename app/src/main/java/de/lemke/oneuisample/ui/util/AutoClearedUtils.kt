package de.lemke.oneuisample.ui.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Property delegate that automatically clears it's value
 * on the Fragment's onDestroyView as required in
 * https://developer.android.com/topic/libraries/view-binding#fragments
 */
fun <T> Fragment.autoCleared(initialize: () -> T): ReadOnlyProperty<Fragment, T> =
    object : ReadOnlyProperty<Fragment, T>, DefaultLifecycleObserver {
        private var cachedValue: T? = null

        override fun onDestroy(owner: LifecycleOwner) {
            cachedValue = null
        }

        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>,
        ): T =
            cachedValue ?: initialize().also {
                cachedValue = it
                viewLifecycleOwner.lifecycle.addObserver(this)
            }
    }
