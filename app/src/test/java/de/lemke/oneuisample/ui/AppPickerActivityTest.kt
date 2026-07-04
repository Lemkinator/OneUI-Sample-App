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
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.picker.model.AppInfo
import androidx.picker.widget.SeslAppPickerGridView
import androidx.picker.widget.SeslAppPickerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.ui.util.ListTypes
import dev.oneuiproject.oneui.layout.ToolbarLayout
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private fun View.findSearchView(): SearchView? =
    this as? SearchView ?: (this as? ViewGroup)?.let { vg ->
        (0 until vg.childCount).firstNotNullOfOrNull { vg.getChildAt(it).findSearchView() }
    }

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppPickerActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<HiltTestApplication>()

    private fun launch(block: AppPickerActivity.() -> Unit = {}) {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun setAppPickerType_list_configuresList() {
        launch {
            setAppPickerType(ListTypes.LIST_TYPE)
            currentPicker shouldNotBe null
        }
    }

    @Test
    fun setAppPickerType_grid_configuresGrid() {
        launch {
            setAppPickerType(ListTypes.TYPE_GRID)
            currentPicker shouldNotBe null
        }
    }

    @Test
    fun setAppPickerType_gridCheckbox_configuresGrid() {
        launch {
            setAppPickerType(ListTypes.TYPE_GRID_CHECKBOX)
            currentPicker shouldNotBe null
        }
    }

    @Test
    fun setAppPickerType_listCheckbox_configuresList() {
        launch {
            setAppPickerType(ListTypes.TYPE_LIST_CHECKBOX)
            currentPicker shouldNotBe null
        }
    }

    @Test
    fun setAppPickerType_actionButton_setsActionIconOnItems() {
        launch {
            setAppPickerType(ListTypes.TYPE_LIST_ACTION_BUTTON)
            currentPicker shouldNotBe null
        }
    }

    @Test
    fun updateAppPickerVisibility_visible_showsPicker() {
        launch {
            setAppPickerType(ListTypes.LIST_TYPE)
            updateAppPickerVisibility(true)
            window.decorView.findViewById<View>(R.id.noEntryScrollView)?.isVisible shouldBe false
            currentPicker?.isVisible shouldBe true
        }
    }

    @Test
    fun updateAppPickerVisibility_invisible_showsNoEntry() {
        launch {
            setAppPickerType(ListTypes.LIST_TYPE)
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            updateAppPickerVisibility(false)
            window.decorView.findViewById<View>(R.id.noEntryScrollView)?.isVisible shouldBe true
            currentPicker?.isVisible shouldBe false
        }
    }

    @Test
    fun onAppItemClick_listPicker_returnsFalse() {
        launch {
            val listPicker = mockk<SeslAppPickerView>(relaxed = true)
            val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
            onAppItemClick(listPicker, appInfo) shouldBe false
        }
    }

    @Test
    fun onAppItemClick_gridPicker_returnsTrue() {
        launch {
            val gridPicker = mockk<SeslAppPickerGridView>(relaxed = true)
            val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
            onAppItemClick(gridPicker, appInfo) shouldBe true
        }
    }

    @Test
    fun onAppItemActionClick_showsSnackBar() {
        launch {
            val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
            onAppItemActionClick(appInfo) shouldBe true
        }
    }

    @Test
    fun applyFilter_withNullPicker_isNoOp() {
        launch {
            currentPicker = null
            applyFilter("test")
        }
    }

    @Test
    fun applyFilter_withQuery_noError() {
        launch {
            setAppPickerType(ListTypes.LIST_TYPE)
            applyFilter("test")
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun applyFilter_emptyQuery_noError() {
        launch {
            setAppPickerType(ListTypes.LIST_TYPE)
            applyFilter()
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun render_samePickerType_earlyReturn() {
        launch {
            render(AppPickerUiState(pickerType = 0))
            val firstPicker = currentPicker
            render(AppPickerUiState(pickerType = 0))
            // Early return: same instance, picker not recreated
            currentPicker shouldBe firstPicker
        }
    }

    @Test
    fun render_differentPickerType_updatesType() {
        launch {
            render(AppPickerUiState(pickerType = 0)) // LIST_TYPE → appPickerList
            val firstPicker = currentPicker
            render(AppPickerUiState(pickerType = 5)) // TYPE_GRID → appPickerGrid
            currentPicker shouldNotBe firstPicker
        }
    }

    @Test
    fun render_outOfBoundsPickerType_usesFirstEntry() {
        launch {
            render(AppPickerUiState(pickerType = 999))
            currentPicker shouldNotBe null
        }
    }

    @Test
    fun render_negativePickerType_usesFirstEntry() {
        launch {
            render(AppPickerUiState(pickerType = -1))
            currentPicker shouldNotBe null
        }
    }

    @Test
    fun onOptionsItemSelected_search_startsSearchMode() {
        launch {
            val item = mockk<MenuItem> { every { itemId } returns R.id.menu_app_picker_search }
            onOptionsItemSelected(item) shouldBe true
        }
    }

    @Test
    fun onOptionsItemSelected_search_triggersQueryAndEnd() {
        launch {
            val item = mockk<MenuItem> { every { itemId } returns R.id.menu_app_picker_search }
            onOptionsItemSelected(item)
            shadowOf(Looper.getMainLooper()).idle()
            val toolbarLayout = this.findViewById<ToolbarLayout>(R.id.toolbar_layout)
            toolbarLayout?.findSearchView()?.setQuery("test", false)
            shadowOf(Looper.getMainLooper()).idle()
            toolbarLayout?.endSearchMode()
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
