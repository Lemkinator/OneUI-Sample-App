package de.lemke.oneuisample.ui

import android.content.Intent
import android.os.Looper
import androidx.picker.model.AppInfo
import androidx.picker.widget.SeslAppPickerGridView
import androidx.picker.widget.SeslAppPickerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.ui.util.ListTypes
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppPickerActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: AppPickerActivity.() -> Unit = {}) {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun setAppPickerType_list_configuresList() {
        launch { setAppPickerType(ListTypes.LIST_TYPE) }
    }

    @Test
    fun setAppPickerType_grid_configuresGrid() {
        launch { setAppPickerType(ListTypes.TYPE_GRID) }
    }

    @Test
    fun setAppPickerType_gridCheckbox_configuresGrid() {
        launch { setAppPickerType(ListTypes.TYPE_GRID_CHECKBOX) }
    }

    @Test
    fun setAppPickerType_listCheckbox_configuresList() {
        launch { setAppPickerType(ListTypes.TYPE_LIST_CHECKBOX) }
    }

    @Test
    fun updateAppPickerVisibility_visible_showsPicker() {
        launch { updateAppPickerVisibility(true) }
    }

    @Test
    fun updateAppPickerVisibility_invisible_showsNoEntry() {
        launch {
            updateAppPickerVisibility(false)
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun onAppItemClick_listPicker_returnsFalse() {
        launch {
            val listPicker = mockk<SeslAppPickerView>(relaxed = true)
            val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
            onAppItemClick(listPicker, appInfo)
        }
    }

    @Test
    fun onAppItemClick_gridPicker_returnsTrue() {
        launch {
            val gridPicker = mockk<SeslAppPickerGridView>(relaxed = true)
            val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
            onAppItemClick(gridPicker, appInfo)
        }
    }

    @Test
    fun onAppItemActionClick_showsSnackBar() {
        launch {
            val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
            onAppItemActionClick(appInfo)
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
            render(AppPickerUiState(pickerType = 0))
        }
    }

    @Test
    fun render_differentPickerType_updatesType() {
        launch {
            render(AppPickerUiState(pickerType = 0))
            render(AppPickerUiState(pickerType = 1))
        }
    }

    @Test
    fun render_outOfBoundsPickerType_usesFirstEntry() {
        launch { render(AppPickerUiState(pickerType = 999)) }
    }
}
