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
package de.lemke.oneuisample.ui

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.viewpager2.widget.ViewPager2
import de.lemke.oneuisample.R
import de.lemke.oneuisample.ui.fragments.TabDesignFragment
import org.robolectric.Shadows.shadowOf

/**
 * Launches [MainActivity], pages the Design tab's ViewPager2 to [subtabIndex], resolves the
 * live [F] subtab fragment instance, and runs [block] against it. [afterBlock] then runs with the
 * still-open [ActivityScenario], outside the `onActivity` callback — needed for scenario-level
 * calls like `moveToState` that must not run while already on the main thread inside `onActivity`.
 */
internal inline fun <reified F : Fragment> withDesignSubtabFragment(
    context: Context,
    subtabIndex: Int,
    crossinline block: F.() -> Unit,
    crossinline afterBlock: (ActivityScenario<MainActivity>) -> Unit = {},
) {
    ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
        shadowOf(Looper.getMainLooper()).idle()
        scenario.onActivity { activity ->
            val tabDesignFragment =
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .childFragmentManager
                    .primaryNavigationFragment as? TabDesignFragment
            tabDesignFragment?.view?.findViewById<ViewPager2>(R.id.viewPager2Design)?.setCurrentItem(subtabIndex, false)
            shadowOf(Looper.getMainLooper()).idle()
            val fragment =
                tabDesignFragment
                    ?.childFragmentManager
                    ?.fragments
                    ?.filterIsInstance<F>()
                    ?.firstOrNull()
            checkNotNull(fragment) { "Expected fragment of type ${F::class.simpleName} not found" }.block()
        }
        shadowOf(Looper.getMainLooper()).idle()
        afterBlock(scenario)
    }
}
