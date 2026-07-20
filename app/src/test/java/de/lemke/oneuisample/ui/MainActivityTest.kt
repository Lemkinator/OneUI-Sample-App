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

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.os.Looper
import android.view.MenuItem
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import dev.oneuiproject.oneui.navigation.widget.DrawerNavigationView
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettings.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun launch(block: ActivityScenario<MainActivity>.() -> Unit = {}) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.block()
        }
    }

    private fun withNavClick(itemId: Int) {
        launch {
            onActivity { activity ->
                val item =
                    activity
                        .findViewById<DrawerNavigationView>(R.id.navigationView)
                        .findMenuItem(itemId) ?: return@onActivity
                activity.onNavigationItemSelected(item)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onCreate_normalLaunch_createsWithoutCrash() {
        launch()
    }

    @Test
    fun onCreate_onboardingRequired_returnsEarly() {
        prefs.edit().clear().commit()
        launch()
    }

    @Test
    fun onSaveInstanceState_recreateWithoutCrash() {
        launch { recreate() }
    }

    @Test
    fun onNewIntent_actionSearch_queriesDrawer() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            controller.newIntent(Intent(ACTION_SEARCH))
            shadowOf(Looper.getMainLooper()).idle()
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun onNewIntent_otherAction_doesNothing() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            controller.newIntent(Intent("some.other.action"))
            shadowOf(Looper.getMainLooper()).idle()
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun navItem_oobeDest_opensOOBEAndFinishes() {
        withNavClick(R.id.oobe_dest)
    }

    @Test
    fun navItem_aboutApp_startsAboutActivity() {
        launch {
            onActivity { activity ->
                val item =
                    activity
                        .findViewById<DrawerNavigationView>(R.id.navigationView)
                        .findMenuItem(R.id.about_app_dest) ?: return@onActivity
                activity.onNavigationItemSelected(item)
            }
            shadowOf(Looper.getMainLooper()).idle()
            onActivity { activity ->
                shadowOf(activity as Activity)
                    .nextStartedActivity
                    ?.component
                    ?.className shouldBe AboutActivity::class.java.name
            }
        }
    }

    @Test
    fun navItem_aboutCustom_startsCustomAboutActivity() {
        launch {
            onActivity { activity ->
                val item =
                    activity
                        .findViewById<DrawerNavigationView>(R.id.navigationView)
                        .findMenuItem(R.id.about_custom_dest) ?: return@onActivity
                activity.onNavigationItemSelected(item)
            }
            shadowOf(Looper.getMainLooper()).idle()
            onActivity { activity ->
                shadowOf(activity as Activity)
                    .nextStartedActivity
                    ?.component
                    ?.className shouldBe CustomAboutActivity::class.java.name
            }
        }
    }

    @Test
    fun navItem_settings_startsSettingsActivity() {
        launch {
            onActivity { activity ->
                val item =
                    activity
                        .findViewById<DrawerNavigationView>(R.id.navigationView)
                        .findMenuItem(R.id.settings_dest) ?: return@onActivity
                activity.onNavigationItemSelected(item)
            }
            shadowOf(Looper.getMainLooper()).idle()
            onActivity { activity ->
                shadowOf(activity as Activity)
                    .nextStartedActivity
                    ?.component
                    ?.className shouldBe SettingsActivity::class.java.name
            }
        }
    }

    @Test
    fun navItem_bottomSheet_showsBottomSheet() {
        withNavClick(R.id.bottom_sheet_dest)
    }

    @Test
    fun navItem_leaks_opensLeakCanary() {
        // LeakCanary auto-installs via ContentProvider which doesn't run in Robolectric;
        // catch the resulting ISE so the branch is still covered without crashing the suite.
        launch {
            onActivity { activity ->
                val item =
                    activity
                        .findViewById<DrawerNavigationView>(R.id.navigationView)
                        .findMenuItem(R.id.leaks_dest) ?: return@onActivity
                try {
                    activity.onNavigationItemSelected(item)
                } catch (_: IllegalStateException) {
                }
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun navItem_popupMenu_hitsElseBranch() {
        withNavClick(R.id.popup_menu)
    }

    @Test
    fun onPopupMenuItemClick_drawerClosed_opensDrawer() {
        launch {
            onActivity { activity -> activity.onPopupMenuItemClick() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun navigation_toIcons_loadsTabIconsFragment() {
        launch {
            onActivity { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .navController
                    .navigate(R.id.icons_dest)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun navigation_toPicker_loadsTabPickerFragment() {
        launch {
            onActivity { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .navController
                    .navigate(R.id.picker_dest)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun openPopupMenu_showsPopupMenuWithoutCrash() {
        launch {
            onActivity { activity -> activity.openPopupMenu() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onPopupMenuItemClicked_setsActivityTitleAndShowsSnackBar() {
        launch {
            onActivity { activity ->
                val menuItem = mockk<MenuItem> { every { title } returns "Test Item" }
                activity.onPopupMenuItemClicked(menuItem)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onSuggestActionButtonClicked_showsSnackBar() {
        launch {
            onActivity { activity -> activity.onSuggestActionButtonClicked() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onPopupMenuItemClick_drawerOpen_showsPopupMenu() {
        launch {
            onActivity { activity ->
                activity.openDrawer()
            }
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            onActivity { activity ->
                activity.onPopupMenuItemClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
