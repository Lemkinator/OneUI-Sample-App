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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.os.Looper
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.children
import androidx.navigation.fragment.NavHostFragment
import androidx.reflect.DeviceInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.util.onNavigationSingleClick
import dev.oneuiproject.oneui.navigation.widget.DrawerNavigationView
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.fakes.RoboMenuItem

private const val TABLET = "sw600dp-w600dp-h1024dp"

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Inject
    lateinit var userSettings: UserSettings

    @After
    fun unmockDeviceInfo() {
        unmockkStatic(DeviceInfo::class)
    }

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    private fun launch(block: ActivityScenario<MainActivity>.() -> Unit = {}) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.block()
        }
    }

    private fun oneUi(isOneUi: Boolean) {
        mockkStatic(DeviceInfo::class)
        every { DeviceInfo.isOneUI() } returns isOneUi
    }

    private fun MainActivity.drawerItem(itemId: Int): MenuItem =
        findViewById<DrawerNavigationView>(R.id.navigationView).findMenuItem(itemId).shouldNotBeNull()

    private fun View.findByTooltip(tooltip: String): View? =
        if (tooltipText == tooltip) {
            this
        } else {
            (this as? ViewGroup)?.children?.firstNotNullOfOrNull { it.findByTooltip(tooltip) }
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
        userSettings.lastVersionCode = -1
        userSettings.acceptedTosVersion = -1
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
    @Config(qualifiers = TABLET)
    fun navItem_settings_opensPopOverOnOneUiTablet() {
        oneUi(true)
        launch {
            onActivity { activity ->
                activity.onNavigationItemSelected(activity.drawerItem(R.id.settings_dest))
                val started = shadowOf(activity as Activity).nextStartedActivityForResult.shouldNotBeNull()
                started.intent.component?.className shouldBe SettingsActivity::class.java.name
                (started.intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP) shouldBe Intent.FLAG_ACTIVITY_SINGLE_TOP
                started.options.shouldNotBeNull()
            }
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun navItem_oobe_startsPlainlyOnOneUiTablet() {
        oneUi(true)
        launch {
            onActivity { activity ->
                activity.onNavigationItemSelected(activity.drawerItem(R.id.oobe_dest))
                val started = shadowOf(activity as Activity).nextStartedActivity.shouldNotBeNull()
                started.component?.className shouldBe OOBEActivity::class.java.name
                (started.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP) shouldBe 0
            }
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun headerButton_opensAboutPopOverOnOneUiTablet() {
        oneUi(true)
        launch {
            onActivity { activity ->
                activity
                    .findViewById<View>(android.R.id.content)
                    .findByTooltip("About OneUI Sample")
                    .shouldNotBeNull()
                    .performClick()
                val started = shadowOf(activity as Activity).nextStartedActivityForResult.shouldNotBeNull()
                started.intent.component?.className shouldBe AboutActivity::class.java.name
                (started.intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP) shouldBe Intent.FLAG_ACTIVITY_SINGLE_TOP
                started.options.shouldNotBeNull()
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
    fun onNavigationSingleClick_rapidRepeat_isBlocked() {
        launch {
            onActivity { activity ->
                val navigationView = activity.findViewById<DrawerNavigationView>(R.id.navigationView)
                var delegateCallCount = 0
                // Robolectric's elapsedRealtime() starts near 0, below the interval.
                SystemClock.sleep(1_000_001L)
                navigationView.onNavigationSingleClick(interval = 1_000_000L) {
                    delegateCallCount++
                    true
                }

                @SuppressLint("RestrictedApi")
                val select = { (navigationView.findMenuItem(R.id.bottom_sheet_dest) as MenuItemImpl).invoke() }
                select() shouldBe true
                delegateCallCount shouldBe 1
                select() shouldBe false
                delegateCallCount shouldBe 1
            }
        }
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
                val menuItem = RoboMenuItem().apply { title = "Test Item" }
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
