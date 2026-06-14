/*
 * Copyright 2024-2026 Leonard Lemke
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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.MenuItem
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.fragments.TabIconsFragment
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabIconsFragmentTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withFragment(block: TabIconsFragment.() -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .navController
                    .navigate(R.id.icons_dest)
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val fragment =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager
                        .primaryNavigationFragment as? TabIconsFragment
                fragment?.block()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun mockMenuItem(id: Int): MenuItem = mockk { every { itemId } returns id }

    @Test
    fun onIconTabMenuItemSelected_search_startsSearchMode() {
        withFragment { onIconTabMenuItemSelected(mockMenuItem(R.id.menu_item_search)) }
    }

    @Test
    fun onIconTabMenuItemSelected_settings_showsDialog() {
        withFragment { onIconTabMenuItemSelected(mockMenuItem(R.id.menu_item_settings)) }
    }

    @Test
    fun onIconTabMenuItemSelected_unknown_returnsFalse() {
        withFragment { onIconTabMenuItemSelected(mockMenuItem(-1)) }
    }

    @Test
    fun onActionModeMenuItemSelected_item1_showsSnackbar() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(R.id.menu_item_1)) }
    }

    @Test
    fun onActionModeMenuItemSelected_item2_showsSnackbar() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(R.id.menu_item_2)) }
    }

    @Test
    fun onActionModeMenuItemSelected_item3_showsSnackbar() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(R.id.menu_item_3)) }
    }

    @Test
    fun onActionModeMenuItemSelected_unknown_returnsFalse() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(-1)) }
    }
}
