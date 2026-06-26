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
            check(thisRef.view != null) {
                "${property.name} accessed after onDestroyView — do not access view-bound properties outside the view lifecycle"
            }
            val value = initialize()
            // ON_DESTROY fires before onDestroyView() in modern AndroidX. If binding is accessed
            // during cleanup, skip caching — adding an observer to a DESTROYED lifecycle is a no-op.
            val lifecycle = thisRef.viewLifecycleOwner.lifecycle
            if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
                cachedValue = value
                lifecycle.addObserver(this)
            }
            return value
        }
    }
