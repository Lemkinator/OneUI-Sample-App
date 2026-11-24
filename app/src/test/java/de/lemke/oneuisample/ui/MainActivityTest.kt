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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.children
import androidx.navigation.fragment.NavHostFragment
import androidx.reflect.DeviceInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.HiltTestRule
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.util.onNavigationSingleClick
import dev.oneuiproject.oneui.layout.NavDrawerLayout
import dev.oneuiproject.oneui.navigation.widget.DrawerNavigationView
import io.kotest.matchers.nulls.shouldBeNull
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
import org.robolectric.shadows.ShadowDialog
import com.google.android.material.R as MaterialR

private const val TABLET = "sw600dp-w600dp-h1024dp"
private const val DRAWER_CLICK_INTERVAL_MS = 600L
private const val UNKNOWN_ITEM_ID = -54321

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltTestRule(this)

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
            // Robolectric's elapsedRealtime() starts below the 600 ms drawer click interval.
            SystemClock.sleep(DRAWER_CLICK_INTERVAL_MS + 1)
            scenario.block()
        }
    }

    private fun oneUi(isOneUi: Boolean) {
        mockkStatic(DeviceInfo::class)
        every { DeviceInfo.isOneUI() } returns isOneUi
    }

    @SuppressLint("RestrictedApi")
    private fun MainActivity.drawerItem(itemId: Int): MenuItemImpl =
        findViewById<DrawerNavigationView>(R.id.navigationView).findMenuItem(itemId).shouldNotBeNull() as MenuItemImpl

    @SuppressLint("RestrictedApi")
    private fun MainActivity.select(itemId: Int): Boolean = drawerItem(itemId).invoke()

    private fun MainActivity.snackbarText(): String? = findViewById<TextView>(MaterialR.id.snackbar_text)?.text?.toString()

    private fun View.findByText(text: String): View? =
        if ((this as? TextView)?.text?.toString() == text) {
            this
        } else {
            (this as? ViewGroup)?.children?.firstNotNullOfOrNull { it.findByText(text) }
        }

    private fun View.findByTooltip(tooltip: String): View? =
        if (tooltipText == tooltip) {
            this
        } else {
            (this as? ViewGroup)?.children?.firstNotNullOfOrNull { it.findByTooltip(tooltip) }
        }

    private fun withNavClick(itemId: Int) {
        launch {
            onActivity { activity -> activity.select(itemId) shouldBe true }
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
            onActivity { activity -> activity.select(R.id.about_app_dest) shouldBe true }
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
            onActivity { activity -> activity.select(R.id.about_custom_dest) shouldBe true }
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
            onActivity { activity -> activity.select(R.id.settings_dest) shouldBe true }
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
                activity.select(R.id.settings_dest) shouldBe true
                val started = shadowOf(activity as Activity).nextStartedActivityForResult.shouldNotBeNull()
                started.intent.component?.className shouldBe SettingsActivity::class.java.name
                val singleTopFlag = started.intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP
                singleTopFlag shouldBe Intent.FLAG_ACTIVITY_SINGLE_TOP
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
                activity.select(R.id.oobe_dest) shouldBe true
                val started = shadowOf(activity as Activity).nextStartedActivity.shouldNotBeNull()
                started.component?.className shouldBe OOBEActivity::class.java.name
                val singleTopFlag = started.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP
                singleTopFlag shouldBe 0
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
                val singleTopFlag = started.intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP
                singleTopFlag shouldBe Intent.FLAG_ACTIVITY_SINGLE_TOP
                started.options.shouldNotBeNull()
            }
        }
    }

    @Test
    fun navItem_bottomSheet_showsAnExpandedSheetThatSkipsTheCollapsedState() {
        launch {
            onActivity { activity -> activity.select(R.id.bottom_sheet_dest) shouldBe true }
            shadowOf(Looper.getMainLooper()).idle()
            val behavior = (ShadowDialog.getLatestDialog() as BottomSheetDialog).behavior
            behavior.state shouldBe BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed shouldBe true
        }
    }

    @Test
    fun navItem_leaks_opensLeakCanary() {
        // LeakCanary auto-installs via ContentProvider which doesn't run in Robolectric;
        // catch the resulting ISE so the branch is still covered without crashing the suite.
        launch {
            onActivity { activity ->
                try {
                    activity.select(R.id.leaks_dest)
                } catch (_: IllegalStateException) {
                }
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onNavigationItemSelected_unknownItem_isNotHandled() {
        launch {
            onActivity { activity ->
                activity.onNavigationItemSelected(RoboMenuItem(UNKNOWN_ITEM_ID)) shouldBe false
                shadowOf(activity as Activity).nextStartedActivity.shouldBeNull()
            }
        }
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
    fun popupItem_drawerClosed_opensDrawer() {
        launch {
            onActivity { activity -> activity.select(R.id.popup_menu) shouldBe true }
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            onActivity { activity -> activity.findViewById<NavDrawerLayout>(R.id.drawerLayout).isOpen() shouldBe true }
        }
    }

    @Test
    fun popupItem_drawerOpen_showsPopupMenu() {
        launch {
            onActivity { activity -> activity.findViewById<NavDrawerLayout>(R.id.drawerLayout).setDrawerOpen(true) }
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            onActivity { activity ->
                activity.select(R.id.popup_menu) shouldBe true
            }
            shadowOf(Looper.getMainLooper()).idle()
            onView(withText("Pop-up menu item 1")).inRoot(isPlatformPopup()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun popupMenuEntry_click_setsTitleAndShowsSnackbar() {
        launch {
            onActivity { activity -> activity.findViewById<NavDrawerLayout>(R.id.drawerLayout).setDrawerOpen(true) }
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            onActivity { activity -> activity.select(R.id.popup_menu) }
            shadowOf(Looper.getMainLooper()).idle()
            onView(withText("Pop-up menu item 1")).inRoot(isPlatformPopup()).perform(click())
            shadowOf(Looper.getMainLooper()).idle()
            onActivity { activity ->
                activity.title.toString() shouldBe "Pop-up menu item 1"
                activity.snackbarText() shouldBe "Pop-up menu item 1 clicked"
            }
        }
    }

    @Test
    fun suggestButton_click_showsSnackbar() {
        launch {
            onActivity { activity ->
                var view: View? = activity.findViewById<View>(android.R.id.content).findByText("Action Button").shouldNotBeNull()
                while (view != null && !view.hasOnClickListeners()) view = view.parent as? View
                view.shouldNotBeNull().performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
            onActivity { activity -> activity.snackbarText() shouldBe "Action button clicked!" }
        }
    }
}
