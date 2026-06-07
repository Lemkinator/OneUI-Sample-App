package de.lemke.oneuisample.ui.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
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
        ): T {
            val current = cachedValue
            if (current != null) return current
            val value = initialize()
            // ON_DESTROY fires before onDestroyView() in modern AndroidX. If binding is accessed
            // during cleanup, skip caching — adding an observer to a DESTROYED lifecycle is a no-op.
            if (thisRef.view != null) {
                val lifecycle = thisRef.viewLifecycleOwner.lifecycle
                if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
                    cachedValue = value
                    lifecycle.addObserver(this)
                }
            }
            return value
        }
    }
