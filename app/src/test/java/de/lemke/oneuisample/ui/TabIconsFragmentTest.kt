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

import android.app.Application
import android.content.Intent
import android.os.Looper
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.fragments.TabIconsFragment
import de.lemke.oneuisample.ui.util.IconAdapter
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.widget.RadioItemViewGroup
import dev.oneuiproject.oneui.widget.SwitchItemView
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowDialog
import com.google.android.material.R as MaterialR
import dev.oneuiproject.oneui.design.R as designR

private fun View.findSearchView(): SearchView? =
    this as? SearchView ?: (this as? ViewGroup)?.children?.firstNotNullOfOrNull { it.findSearchView() }

private fun View.findByTitle(title: String): View? =
    if (tooltipText?.toString() == title || contentDescription?.toString() == title) {
        this
    } else {
        (this as? ViewGroup)?.children?.firstNotNullOfOrNull { it.findByTitle(title) }
    }

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabIconsFragmentTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Inject
    lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
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
                requireNotNull(fragment) { "TabIconsFragment was not displayed" }.block()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun TabIconsFragment.snackbarText(): String? {
        shadowOf(Looper.getMainLooper()).idle()
        return requireActivity().findViewById<TextView>(MaterialR.id.snackbar_text)?.text?.toString()
    }

    private fun TabIconsFragment.iconList() = requireView().findViewById<RecyclerView>(R.id.iconList)

    private fun TabIconsFragment.adapter() = iconList().adapter as IconAdapter

    private fun TabIconsFragment.drawer() = requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout)

    private fun TabIconsFragment.menu(itemId: Int): Boolean =
        requireActivity().onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, RoboMenuItem(itemId))

    private fun TabIconsFragment.searchView(): SearchView =
        requireActivity().findViewById<View>(android.R.id.content).findSearchView().shouldNotBeNull()

    private fun TabIconsFragment.actionModeMenu() =
        requireActivity().findViewById<Toolbar>(designR.id.toolbarlayout_action_mode_toolbar).menu

    private fun TabIconsFragment.actionModeItem(title: String): View =
        requireActivity().findViewById<View>(android.R.id.content).findByTitle(title).shouldNotBeNull()

    private fun TabIconsFragment.itemView(position: Int): View {
        shadowOf(Looper.getMainLooper()).idle()
        return iconList().findViewHolderForAdapterPosition(position).shouldNotBeNull().itemView
    }

    private fun TabIconsFragment.startActionMode() {
        shadowOf(Looper.getMainLooper()).idle()
        onView(withText(adapter().getItemByPosition(0).name)).perform(longClick())
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun latestDialog(): AlertDialog = ShadowDialog.getLatestDialog() as AlertDialog

    private fun TabIconsFragment.openSettingsDialog(): AlertDialog {
        menu(R.id.menu_item_settings) shouldBe true
        shadowOf(Looper.getMainLooper()).idle()
        return latestDialog()
    }

    private fun AlertDialog.pressApply() {
        getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun menu_search_startsSearchMode() {
        withFragment {
            menu(R.id.menu_item_search) shouldBe true
            drawer().isSearchMode shouldBe true
        }
    }

    @Test
    fun menu_settings_showsTheSettingsDialog() {
        withFragment {
            openSettingsDialog().findViewById<TextView>(androidx.appcompat.R.id.alertTitle)!!.text.toString() shouldBe "Settings"
        }
    }

    @Test
    fun menu_unknownItem_isNotHandled() {
        withFragment { menu(UNKNOWN_ITEM_ID) shouldBe false }
    }

    @Test
    fun search_submit_storesTheQuery() {
        withFragment {
            menu(R.id.menu_item_search)
            searchView().setQuery("query", true)
            userSettings.search shouldBe "query"
        }
    }

    @Test
    fun search_typing_storesTheQuery() {
        withFragment {
            menu(R.id.menu_item_search)
            searchView().setQuery("change", false)
            userSettings.search shouldBe "change"
        }
    }

    @Test
    fun search_clearing_storesAnEmptyQuery() {
        withFragment {
            menu(R.id.menu_item_search)
            searchView().setQuery("change", false)
            searchView().setQuery("", false)
            userSettings.search shouldBe ""
        }
    }

    @Test
    fun search_started_restoresThePreviousQueryAndMarksSearchActive() {
        userSettings.search = "prev"
        withFragment {
            menu(R.id.menu_item_search)
            searchView().query.toString() shouldBe "prev"
            userSettings.searchActive shouldBe true
        }
    }

    @Test
    fun search_ended_marksSearchInactive() {
        withFragment {
            menu(R.id.menu_item_search)
            drawer().endSearchMode()
            shadowOf(Looper.getMainLooper()).idle()
            userSettings.searchActive shouldBe false
        }
    }

    @Test
    fun search_overflowButton_opensTheFilterDialog() {
        withFragment {
            menu(R.id.menu_item_search)
            searchView().findViewById<View>(androidx.appcompat.R.id.search_more_btn).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            latestDialog().findViewById<TextView>(androidx.appcompat.R.id.alertTitle)!!.text.toString() shouldBe "Search Filter"
        }
    }

    @Test
    fun list_withoutSearch_showsEveryIcon() {
        withFragment {
            shadowOf(Looper.getMainLooper()).idle()
            iconList().isVisible shouldBe true
            requireView().findViewById<View>(R.id.noEntryScrollView).isVisible shouldBe false
            adapter().itemCount shouldBe ALL_ICON_COUNT
        }
    }

    @Test
    fun list_withMatchingSearch_showsOnlyMatches() {
        withFragment {
            userSettings.searchActive = true
            userSettings.search = "settings"
            shadowOf(Looper.getMainLooper()).idle()
            adapter().itemCount shouldBe 3
            adapter().getItemByPosition(0).name shouldBe "page_settings"
        }
    }

    @Test
    fun list_withoutMatch_showsNoEntryView() {
        withFragment {
            userSettings.searchActive = true
            userSettings.search = NO_MATCH_QUERY
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            requireView().findViewById<View>(R.id.noEntryScrollView).isVisible shouldBe true
            iconList().isVisible shouldBe false
        }
    }

    @Test
    fun item_click_showsTheBeautifiedNameInASnackBar() {
        withFragment {
            itemView(0).performClick()
            snackbarText() shouldBe "360"
        }
    }

    @Test
    fun item_longClick_startsActionModeAndClickTogglesSelection() {
        withFragment {
            startActionMode()
            drawer().isActionMode shouldBe true
            val itemId = adapter().getItemId(0)
            adapter().isSelected(itemId) shouldBe true
            itemView(0).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            adapter().isSelected(itemId) shouldBe false
            itemView(0).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            adapter().isSelected(itemId) shouldBe true
        }
    }

    @Test
    fun actionMode_selectAll_selectsEveryIcon() {
        withFragment {
            startActionMode()
            requireActivity().findViewById<View>(designR.id.toolbarlayout_selectall).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            adapter().isSelected(adapter().getItemId(0)) shouldBe true
        }
    }

    @Test
    fun actionMode_end_leavesSelectionMode() {
        withFragment {
            startActionMode()
            drawer().endActionMode()
            shadowOf(Looper.getMainLooper()).idle()
            drawer().isActionMode shouldBe false
            adapter().isActionMode shouldBe false
        }
    }

    @Test
    fun actionMode_menuItem1_showsSnackBarAndEndsActionMode() {
        withFragment {
            startActionMode()
            actionModeItem("Item 1").performClick()
            snackbarText() shouldBe "Menu item 1 selected"
            drawer().isActionMode shouldBe false
        }
    }

    @Test
    fun actionMode_menuItem2_showsSnackBarAndEndsActionMode() {
        withFragment {
            startActionMode()
            actionModeItem("Item 2").performClick()
            snackbarText() shouldBe "Menu item 2 selected"
            drawer().isActionMode shouldBe false
        }
    }

    @Test
    fun actionMode_menuItem3_showsSnackBarAndEndsActionMode() {
        withFragment {
            startActionMode()
            actionModeItem(getString(androidx.appcompat.R.string.abc_action_menu_overflow_description)).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            onView(withText("Item 3")).inRoot(isPlatformPopup()).perform(click())
            shadowOf(Looper.getMainLooper()).idle()
            snackbarText() shouldBe "Menu item 3 selected"
            drawer().isActionMode shouldBe false
        }
    }

    @Test
    fun actionMode_unknownMenuItem_keepsActionMode() {
        withFragment {
            startActionMode()
            actionModeMenu().add(Menu.NONE, UNKNOWN_ITEM_ID, Menu.NONE, "unknown")
            actionModeMenu().performIdentifierAction(UNKNOWN_ITEM_ID, 0)
            drawer().isActionMode shouldBe true
        }
    }

    @Test
    fun settingsDialog_dismissPreset_checksTheDismissRadio() {
        userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.Dismiss
        withFragment {
            openSettingsDialog().findViewById<RadioItemViewGroup>(R.id.amsOptions)!!.checkedRadioButtonId shouldBe
                R.id.amsDismiss
        }
    }

    @Test
    fun settingsDialog_noDismissPreset_checksTheNoDismissRadio() {
        userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.NoDismiss
        withFragment {
            openSettingsDialog().findViewById<RadioItemViewGroup>(R.id.amsOptions)!!.checkedRadioButtonId shouldBe R.id.amsNoDismiss
        }
    }

    @Test
    fun settingsDialog_concurrentPreset_checksTheConcurrentRadio() {
        userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.Concurrent(null)
        withFragment {
            openSettingsDialog().findViewById<RadioItemViewGroup>(R.id.amsOptions)!!.checkedRadioButtonId shouldBe R.id.amsConcurrent
        }
    }

    @Test
    fun settingsDialog_dismissRadio_storesDismiss() {
        userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.NoDismiss
        withFragment {
            val dialog = openSettingsDialog()
            dialog.findViewById<RadioItemViewGroup>(R.id.amsOptions)!!.check(R.id.amsDismiss)
            dialog.pressApply()
            userSettings.searchOnActionMode shouldBe ToolbarLayout.SearchOnActionMode.Dismiss
        }
    }

    @Test
    fun settingsDialog_noDismissRadio_storesNoDismiss() {
        userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.Dismiss
        withFragment {
            val dialog = openSettingsDialog()
            dialog.findViewById<RadioItemViewGroup>(R.id.amsOptions)!!.check(R.id.amsNoDismiss)
            dialog.pressApply()
            userSettings.searchOnActionMode shouldBe ToolbarLayout.SearchOnActionMode.NoDismiss
        }
    }

    @Test
    fun settingsDialog_concurrentRadio_storesConcurrent() {
        userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.Dismiss
        withFragment {
            val dialog = openSettingsDialog()
            dialog.findViewById<RadioItemViewGroup>(R.id.amsOptions)!!.check(R.id.amsConcurrent)
            dialog.pressApply()
            userSettings.searchOnActionMode shouldBe ToolbarLayout.SearchOnActionMode.Concurrent(null)
        }
    }

    @Test
    fun settingsDialog_switches_areStoredOnApply() {
        userSettings.actionModeShowCancel = false
        userSettings.showIndexScroll = false
        userSettings.indexScrollShowLetters = false
        userSettings.indexScrollAutoHide = false
        withFragment {
            val dialog = openSettingsDialog()
            dialog.findViewById<SwitchItemView>(R.id.actionModeShowCancel)!!.isChecked = true
            dialog.findViewById<SwitchItemView>(R.id.showIndexScroll)!!.isChecked = true
            dialog.findViewById<SwitchItemView>(R.id.indexScrollShowLetters)!!.isChecked = true
            dialog.findViewById<SwitchItemView>(R.id.indexScrollAutoHide)!!.isChecked = true
            dialog.pressApply()
            listOf(
                userSettings.actionModeShowCancel,
                userSettings.showIndexScroll,
                userSettings.indexScrollShowLetters,
                userSettings.indexScrollAutoHide,
            ) shouldBe listOf(true, true, true, true)
        }
    }

    @Test
    fun settingsDialog_indexScrollSwitch_enablesTheSubOptions() {
        userSettings.showIndexScroll = false
        withFragment {
            val dialog = openSettingsDialog()
            dialog.findViewById<SwitchItemView>(R.id.indexScrollShowLetters)!!.isEnabled shouldBe false
            dialog.findViewById<SwitchItemView>(R.id.indexScrollAutoHide)!!.isEnabled shouldBe false
            dialog.findViewById<SwitchItemView>(R.id.showIndexScroll)!!.performClick()
            dialog.findViewById<SwitchItemView>(R.id.indexScrollShowLetters)!!.isEnabled shouldBe true
            dialog.findViewById<SwitchItemView>(R.id.indexScrollAutoHide)!!.isEnabled shouldBe true
            dialog.findViewById<SwitchItemView>(R.id.showIndexScroll)!!.performClick()
            dialog.findViewById<SwitchItemView>(R.id.indexScrollShowLetters)!!.isEnabled shouldBe false
        }
    }

    @Test
    fun indexScroll_setting_switchesBetweenIndexScrollAndFastScroller() {
        withFragment {
            userSettings.showIndexScroll = true
            shadowOf(Looper.getMainLooper()).idle()
            requireView().findViewById<View>(R.id.iconIndexScroll).isVisible shouldBe true
            iconList().seslIsFastScrollerEnabled() shouldBe false
            userSettings.showIndexScroll = false
            shadowOf(Looper.getMainLooper()).idle()
            iconList().seslIsFastScrollerEnabled() shouldBe true
        }
    }

    @Test
    fun multiSelectTip_withThreeIcons_isShownAfterTheDelay() {
        withFragment {
            shadowOf(Looper.getMainLooper()).idleFor(2, TimeUnit.SECONDS)
            shadowOf(RuntimeEnvironment.getApplication()).latestPopupWindow shouldNotBe null
        }
    }

    @Test
    fun multiSelectTip_withTooFewIcons_isNotShown() {
        withFragment {
            userSettings.searchActive = true
            userSettings.search = FEW_MATCHES_QUERY
            shadowOf(Looper.getMainLooper()).idle()
            shadowOf(Looper.getMainLooper()).idleFor(2, TimeUnit.SECONDS)
            shadowOf(RuntimeEnvironment.getApplication()).latestPopupWindow shouldBe null
        }
    }

    @Test
    fun fab_click_showsTheTip() {
        withFragment {
            requireView().findViewById<View>(R.id.fabIcons).performClick()
            shadowOf(Looper.getMainLooper()).idleFor(2, TimeUnit.SECONDS)
            shadowOf(RuntimeEnvironment.getApplication()).latestPopupWindow shouldNotBe null
        }
    }

    companion object {
        private const val UNKNOWN_ITEM_ID = 12345
        private const val ALL_ICON_COUNT = 883
        private const val NO_MATCH_QUERY = "zzzzzz-no-icon-matches"
        private const val FEW_MATCHES_QUERY = "page_settings"
    }
}
