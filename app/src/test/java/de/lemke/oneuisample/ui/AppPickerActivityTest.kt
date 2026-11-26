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

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Looper
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.picker.model.AppInfoData
import androidx.picker.widget.SeslAppPickerGridView
import androidx.picker.widget.SeslAppPickerSelectLayout
import androidx.picker.widget.SeslAppPickerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.HiltTestRule
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.di.IoDispatcher
import de.lemke.oneuisample.di.PausableTestDispatcher
import de.lemke.oneuisample.ui.util.ListTypes
import dev.oneuiproject.oneui.layout.ToolbarLayout
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem
import com.google.android.material.R as MaterialR

private fun View.findSearchView(): SearchView? =
    this as? SearchView ?: (this as? ViewGroup)?.let { vg ->
        (0 until vg.childCount).firstNotNullOfOrNull { vg.getChildAt(it).findSearchView() }
    }

private const val OUT_OF_RANGE_TYPE = 999
private const val NO_MATCH_QUERY = "zzzzzz-no-app-matches"
private const val ICON_SIZE_PX = 24
private const val ALPHA_SHIFT = 24
private const val OPAQUE_ALPHA = 0xFF
private val LIGHT_ICON_COLOR = 0xFF010101.toInt()
private val NIGHT_ICON_COLOR = 0xFFFAFAFA.toInt()

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppPickerActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltTestRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<HiltTestApplication>()

    @Inject
    lateinit var userSettings: UserSettings

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun resetNightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun launch(block: AppPickerActivity.() -> Unit = {}) {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun AppPickerActivity.list() = findViewById<SeslAppPickerView>(R.id.appPickerList)

    private fun AppPickerActivity.grid() = findViewById<SeslAppPickerGridView>(R.id.appPickerGrid)

    private fun AppPickerActivity.noEntry() = findViewById<View>(R.id.noEntryScrollView)

    private fun AppPickerActivity.selectType(type: ListTypes) {
        findViewById<Spinner>(R.id.appPickerSpinner).setSelection(type.ordinal)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun AppPickerActivity.itemView(
        picker: SeslAppPickerView,
        position: Int,
    ): View = picker.findViewHolderForAdapterPosition(position).shouldNotBeNull().itemView

    private fun AppPickerActivity.snackbarText(): String? = findViewById<TextView>(MaterialR.id.snackbar_text)?.text?.toString()

    private fun AppPickerActivity.searchView(): SearchView =
        findViewById<ToolbarLayout>(R.id.toolbar_layout).findSearchView().shouldNotBeNull()

    private fun AppPickerActivity.search(query: String) {
        onOptionsItemSelected(RoboMenuItem(R.id.menu_app_picker_search)) shouldBe true
        shadowOf(Looper.getMainLooper()).idle()
        searchView().setQuery(query, false)
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()
    }

    private fun AppPickerActivity.toggleLayoutMode() {
        onOptionsItemSelected(RoboMenuItem(R.id.menu_app_picker_layout_mode)) shouldBe true
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun AppPickerActivity.listIsShown() {
        list().isVisible shouldBe true
        grid().isVisible shouldBe false
    }

    private fun AppPickerActivity.gridIsShown() {
        list().isVisible shouldBe false
        grid().isVisible shouldBe true
    }

    @Test
    fun defaultType_showsTheListPicker() {
        launch {
            listIsShown()
            noEntry().isVisible shouldBe false
        }
    }

    @Test
    fun spinner_grid_showsTheGrid() {
        launch {
            selectType(ListTypes.TYPE_GRID)
            gridIsShown()
        }
    }

    @Test
    fun spinner_gridCheckbox_showsTheGrid() {
        launch {
            selectType(ListTypes.TYPE_GRID_CHECKBOX)
            gridIsShown()
        }
    }

    @Test
    fun spinner_listCheckbox_showsTheList() {
        launch {
            selectType(ListTypes.TYPE_GRID)
            selectType(ListTypes.TYPE_LIST_CHECKBOX)
            listIsShown()
        }
    }

    @Test
    fun spinner_listRadioButton_showsTheList() {
        launch {
            selectType(ListTypes.TYPE_GRID)
            selectType(ListTypes.TYPE_LIST_RADIOBUTTON)
            listIsShown()
        }
    }

    @Test
    fun spinner_listSwitch_showsTheList() {
        launch {
            selectType(ListTypes.TYPE_GRID)
            selectType(ListTypes.TYPE_LIST_SWITCH)
            listIsShown()
        }
    }

    @Test
    fun spinner_actionButton_setsActionIconOnItems() {
        launch {
            selectType(ListTypes.TYPE_LIST_ACTION_BUTTON)
            val items = list().appDataList.filterIsInstance<AppInfoData>()
            items.shouldNotBeEmpty()
            items.forEach { it.actionIcon.shouldNotBeNull() }
        }
    }

    @Test
    fun spinner_sameTypeTwice_keepsThePicker() {
        launch {
            selectType(ListTypes.TYPE_GRID)
            selectType(ListTypes.TYPE_GRID)
            gridIsShown()
        }
    }

    @Test
    fun spinner_selection_isStoredInTheSettings() {
        launch {
            selectType(ListTypes.TYPE_GRID_CHECKBOX)
            userSettings.appPickerType shouldBe ListTypes.TYPE_GRID_CHECKBOX.ordinal
        }
    }

    @Test
    fun storedType_pickerIsShownOnLaunch() {
        userSettings.appPickerType = ListTypes.TYPE_GRID.ordinal
        launch { gridIsShown() }
    }

    @Test
    fun storedTypeBeyondTheEntries_fallsBackToTheList() {
        userSettings.appPickerType = OUT_OF_RANGE_TYPE
        launch { listIsShown() }
    }

    @Test
    fun storedNegativeType_fallsBackToTheList() {
        userSettings.appPickerType = -1
        launch { listIsShown() }
    }

    @Test
    fun search_withoutMatch_showsNoEntry() {
        launch {
            search(NO_MATCH_QUERY)
            noEntry().isVisible shouldBe true
            list().isVisible shouldBe false
        }
    }

    @Test
    fun search_withMatch_keepsTheList() {
        launch {
            search("OneUI")
            noEntry().isVisible shouldBe false
            list().isVisible shouldBe true
        }
    }

    @Test
    fun search_ended_showsTheListAgain() {
        launch {
            search(NO_MATCH_QUERY)
            findViewById<ToolbarLayout>(R.id.toolbar_layout).endSearchMode()
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            noEntry().isVisible shouldBe false
            list().isVisible shouldBe true
            findViewById<Spinner>(R.id.appPickerSpinner).isEnabled shouldBe true
        }
    }

    @Test
    fun search_started_disablesTheSpinner() {
        launch {
            search("OneUI")
            findViewById<Spinner>(R.id.appPickerSpinner).isEnabled shouldBe false
        }
    }

    @Test
    fun listItem_click_showsSnackBar() {
        launch {
            itemView(list(), 0).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbarText() shouldBe "OneUI Sample App (Debug) clicked!"
        }
    }

    @Test
    fun gridItem_click_togglesTheSelectionAndShowsSnackBar() {
        launch {
            selectType(ListTypes.TYPE_GRID)
            val item = grid().appDataList.filterIsInstance<AppInfoData>().first()
            item.selected shouldBe false
            itemView(grid(), 0).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            grid()
                .appDataList
                .filterIsInstance<AppInfoData>()
                .first()
                .selected shouldBe true
            snackbarText() shouldBe "OneUI Sample App (Debug) clicked!"
        }
    }

    @Test
    fun actionButton_click_showsSnackBar() {
        launch {
            selectType(ListTypes.TYPE_LIST_ACTION_BUTTON)
            itemView(list(), 0).findViewById<View>(androidx.picker.R.id.image_button).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbarText() shouldBe "OneUI Sample App (Debug) action clicked!"
        }
    }

    @Test
    fun checkboxList_appClick_selectsTheApp() {
        launch {
            selectType(ListTypes.TYPE_LIST_CHECKBOX)
            list().appDataList.filterIsInstance<AppInfoData>().map { it.selected } shouldBe listOf(false)
            itemView(list(), 0).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            list().appDataList.filterIsInstance<AppInfoData>().map { it.selected } shouldBe listOf(true)
        }
    }

    @Test
    fun checkboxList_selection_survivesStopAndStart() {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                it.selectType(ListTypes.TYPE_LIST_CHECKBOX)
                it.itemView(it.list(), 0).performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                it
                    .list()
                    .appDataList
                    .filterIsInstance<AppInfoData>()
                    .map { item -> item.selected } shouldBe listOf(true)
            }
        }
    }

    @Test
    fun layoutMode_toggle_showsTheSelectLayoutAndRestoresThePicker() {
        launch {
            toggleLayoutMode()
            findViewById<View>(R.id.appPickerSelectLayout).isVisible shouldBe true
            findViewById<View>(R.id.appPickerSpinner).isVisible shouldBe false
            list().isVisible shouldBe false
            noEntry().isVisible shouldBe false
            toggleLayoutMode()
            findViewById<View>(R.id.appPickerSelectLayout).isVisible shouldBe false
            findViewById<View>(R.id.appPickerSpinner).isVisible shouldBe true
            listIsShown()
        }
    }

    @Test
    fun layoutMode_search_filtersTheSelectLayout() {
        launch {
            toggleLayoutMode()
            search("OneUI")
            findViewById<View>(R.id.appPickerSelectLayout).isVisible shouldBe true
        }
    }

    @Test
    fun onOptionsItemSelected_search_startsSearchMode() {
        launch {
            val item = RoboMenuItem(R.id.menu_app_picker_search)
            onOptionsItemSelected(item) shouldBe true
            shadowOf(Looper.getMainLooper()).idle()
            findViewById<ToolbarLayout>(R.id.toolbar_layout).isSearchMode shouldBe true
        }
    }

    @Test
    fun onOptionsItemSelected_layoutMode_togglesSelectLayoutModeAndUpdatesTitle() {
        launch {
            val item = RoboMenuItem(R.id.menu_app_picker_layout_mode)
            onOptionsItemSelected(item) shouldBe true
            item.title shouldBe getString(R.string.simple_picker_mode)
            onOptionsItemSelected(item) shouldBe true
            item.title shouldBe getString(R.string.select_layout_mode)
        }
    }

    @Test
    fun onPrepareOptionsMenu_simpleMode_showsSelectLayoutTitle() {
        launch {
            val menu = RoboMenu(this)
            val menuItem = menu.add(Menu.NONE, R.id.menu_app_picker_layout_mode, Menu.NONE, "")
            onPrepareOptionsMenu(menu)
            menuItem.title shouldBe getString(R.string.select_layout_mode)
        }
    }

    @Test
    fun onPrepareOptionsMenu_selectLayoutMode_showsSimpleModeTitle() {
        launch {
            val toggleItem = RoboMenuItem(R.id.menu_app_picker_layout_mode)
            onOptionsItemSelected(toggleItem)
            shadowOf(Looper.getMainLooper()).idle()
            val menu = RoboMenu(this)
            val menuItem = menu.add(Menu.NONE, R.id.menu_app_picker_layout_mode, Menu.NONE, "")
            onPrepareOptionsMenu(menu)
            menuItem.title shouldBe getString(R.string.simple_picker_mode)
        }
    }

    @Test
    fun onPrepareOptionsMenu_missingMenuItem_showsMenu() {
        launch {
            val menu = RoboMenu(this)
            onPrepareOptionsMenu(menu) shouldBe true
            menu.size() shouldBe 0
        }
    }

    private val io get() = ioDispatcher as PausableTestDispatcher

    private fun pausedIo() = io.also { it.paused = true }

    private fun AppPickerActivity.progressVisible() = findViewById<View>(R.id.app_picker_progress).isVisible

    @Test
    fun selectLayout_load_runsThroughInjectedDispatcher() {
        launch {
            val io = pausedIo()
            onOptionsItemSelected(RoboMenuItem(R.id.menu_app_picker_layout_mode))
            shadowOf(Looper.getMainLooper()).idle()
            io.heldCount shouldBe 1
            progressVisible() shouldBe true
            io.resume()
            shadowOf(Looper.getMainLooper()).idle()
            io.heldCount shouldBe 0
            progressVisible() shouldBe false
        }
    }

    @Test
    fun selectLayout_reentry_loadsAgain() {
        launch {
            val io = pausedIo()
            val toggle = RoboMenuItem(R.id.menu_app_picker_layout_mode)
            onOptionsItemSelected(toggle)
            shadowOf(Looper.getMainLooper()).idle()
            io.resume()
            shadowOf(Looper.getMainLooper()).idle()
            onOptionsItemSelected(toggle)
            shadowOf(Looper.getMainLooper()).idle()
            io.paused = true
            onOptionsItemSelected(toggle)
            shadowOf(Looper.getMainLooper()).idle()
            io.heldCount shouldBe 1
            progressVisible() shouldBe true
        }
    }

    @Test
    fun recreate_keepsTheListAndTheSelectionsWithoutLoadingAgain() {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                it.selectType(ListTypes.TYPE_LIST_CHECKBOX)
                it.itemView(it.list(), 0).performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
            val loads = io.dispatchCount
            scenario.recreate()
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.list().selectedStates() shouldBe listOf(true) }
            io.dispatchCount shouldBe loads
        }
    }

    @Test
    fun selectLayout_recreate_keepsTheListWithoutLoadingAgain() {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                it.toggleLayoutMode()
                it.itemView(it.selectLayoutList(), 0).performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
            val loads = io.dispatchCount
            scenario.recreate()
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                it.findViewById<View>(R.id.appPickerSelectLayout).isVisible shouldBe true
                it.progressVisible() shouldBe false
                it.selectLayoutList().selectedStates() shouldBe listOf(true)
            }
            io.dispatchCount shouldBe loads
        }
    }

    @Test
    fun simpleMode_typeChange_showsTheProgressUntilTheNewListIsLoaded() {
        launch {
            val io = pausedIo()
            selectType(ListTypes.TYPE_GRID)
            io.heldCount shouldBe 1
            progressVisible() shouldBe true
            io.resume()
            shadowOf(Looper.getMainLooper()).idle()
            progressVisible() shouldBe false
            gridIsShown()
            grid().appDataList.filterIsInstance<AppInfoData>().map { it.packageName } shouldBe listOf("de.lemke.oneuisample.debug")
        }
    }

    @Test
    fun simpleMode_searchDuringTypeChangeLoad_filtersTheNewGrid() {
        launch {
            val io = pausedIo()
            selectType(ListTypes.TYPE_GRID)
            io.heldCount shouldBe 1
            search(NO_MATCH_QUERY)
            io.resume()
            shadowOf(Looper.getMainLooper()).idle()
            noEntry().isVisible shouldBe true
            grid().isVisible shouldBe false
        }
    }

    @Test
    fun simpleMode_searchEnded_leavesNoFilterOnTheOtherPicker() {
        launch {
            val io = pausedIo()
            selectType(ListTypes.TYPE_GRID)
            io.heldCount shouldBe 1
            search(NO_MATCH_QUERY)
            io.resume()
            shadowOf(Looper.getMainLooper()).idle()
            findViewById<ToolbarLayout>(R.id.toolbar_layout).endSearchMode()
            shadowOf(Looper.getMainLooper()).idle()
            selectType(ListTypes.TYPE_LIST_CHECKBOX)
            shadowOf(Looper.getMainLooper()).idle()
            listIsShown()
            noEntry().isVisible shouldBe false
            list().adapter.shouldNotBeNull().itemCount shouldBe 1
            list().appDataList.filterIsInstance<AppInfoData>().map { it.packageName } shouldBe listOf("de.lemke.oneuisample.debug")
        }
    }

    @Test
    fun layoutMode_toggleOff_reloadsTheSimpleListWithResetSelections() {
        launch {
            selectType(ListTypes.TYPE_LIST_CHECKBOX)
            itemView(list(), 0).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            list().selectedStates() shouldBe listOf(true)
            toggleLayoutMode()
            toggleLayoutMode()
            listIsShown()
            list().selectedStates() shouldBe listOf(false)
        }
    }

    @Test
    fun actionIcon_followsTheNightModeOfARecreate() {
        userSettings.appPickerType = ListTypes.TYPE_LIST_ACTION_BUTTON.ordinal
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                it.list().actionIconColor() shouldBe LIGHT_ICON_COLOR
                it.viewModelApps().shouldNotBeEmpty().forEach { app -> app.actionIcon.shouldBeNull() }
            }
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            scenario.recreate()
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                it.resources.configuration.isNightModeActive shouldBe true
                it.list().actionIconColor() shouldBe NIGHT_ICON_COLOR
                it.viewModelApps().shouldNotBeEmpty().forEach { app -> app.actionIcon.shouldBeNull() }
            }
        }
    }

    private fun AppPickerActivity.viewModelApps() = (loadedApps() as AppListState.Loaded).apps

    private fun AppPickerActivity.loadedApps() = ViewModelProvider(this)[AppPickerViewModel::class.java].state.value.apps

    private fun AppPickerActivity.selectLayoutList() =
        findViewById<SeslAppPickerSelectLayout>(R.id.appPickerSelectLayout).appPickerStateView

    private fun SeslAppPickerView.selectedStates() = appDataList.filterIsInstance<AppInfoData>().map { it.selected }

    private fun SeslAppPickerView.actionIconColor(): Int {
        val icon: Drawable =
            appDataList
                .filterIsInstance<AppInfoData>()
                .first()
                .actionIcon
                .shouldNotBeNull()
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        icon.setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
        icon.draw(Canvas(bitmap))
        return (0 until ICON_SIZE_PX * ICON_SIZE_PX)
            .map { bitmap.getPixel(it % ICON_SIZE_PX, it / ICON_SIZE_PX) }
            .first { it ushr ALPHA_SHIFT == OPAQUE_ALPHA }
    }
}
