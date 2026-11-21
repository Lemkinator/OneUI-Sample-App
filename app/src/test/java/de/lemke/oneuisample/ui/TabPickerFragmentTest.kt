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
import android.content.res.Configuration
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.picker.app.SeslDatePickerDialog
import androidx.picker.app.SeslTimePickerDialog
import androidx.picker.widget.SeslNumberPicker
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.fragments.TabPickerFragment
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import com.google.android.material.R as MaterialR

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabPickerFragmentTest {
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

    private fun withFragment(block: TabPickerFragment.() -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .navController
                    .navigate(R.id.picker_dest)
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val fragment =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager
                        .primaryNavigationFragment as? TabPickerFragment
                fragment?.block()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun withFragmentController(block: (ActivityController<MainActivity>, TabPickerFragment) -> Unit) {
        val controller = Robolectric.buildActivity(MainActivity::class.java, Intent(context, MainActivity::class.java)).setup()
        shadowOf(Looper.getMainLooper()).idle()
        try {
            val host = controller.get().supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
            host.navController.navigate(R.id.picker_dest)
            shadowOf(Looper.getMainLooper()).idle()
            block(controller, host.childFragmentManager.primaryNavigationFragment as TabPickerFragment)
        } finally {
            controller.pause().stop().destroy()
        }
    }

    private fun ActivityController<MainActivity>.changeConfiguration() {
        RuntimeEnvironment.setQualifiers("+land")
        configurationChange(Configuration(RuntimeEnvironment.getApplication().resources.configuration))
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun TabPickerFragment.click(viewId: Int) {
        requireView().findViewById<View>(viewId).performClick()
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun latestDialog(): AlertDialog = ShadowDialog.getLatestDialog() as AlertDialog

    private fun confirmLatestDialog() {
        shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS)
        latestDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun TabPickerFragment.snackbarText(): String? =
        requireActivity().findViewById<TextView>(MaterialR.id.snackbar_text)?.text?.toString()

    private fun TabPickerFragment.numberPicker(viewId: Int) = requireView().findViewById<SeslNumberPicker>(viewId)

    @Test
    fun dateButton_confirm_showsPickedDate() {
        withFragment {
            click(R.id.dateButton)
            (latestDialog() as SeslDatePickerDialog).datePicker.updateDate(2025, Calendar.DECEMBER, 25)
            confirmLatestDialog()
            snackbarText() shouldBe "2025-12-25"
        }
    }

    @Test
    fun timeButton_confirm_showsPickedTime() {
        withFragment {
            click(R.id.timeButton)
            (latestDialog() as SeslTimePickerDialog).updateTime(14, 30)
            confirmLatestDialog()
            snackbarText() shouldBe "14:30"
        }
    }

    @Test
    fun startEndTimeButton_confirm_showsDefaultRange() {
        withFragment {
            click(R.id.startEndTimeButton)
            confirmLatestDialog()
            snackbarText() shouldBe "Start time: 00:00\nEnd time: 10:00"
        }
    }

    @Test
    fun colorButton_confirm_keepsThePresetColorAsCurrentAndFirstRecent() {
        userSettings.currentColor = PRESET_COLOR
        userSettings.recentColors = listOf(PRESET_COLOR, OTHER_COLOR)
        withFragment {
            click(R.id.colorButton)
            confirmLatestDialog()
            userSettings.currentColor shouldBe PRESET_COLOR
            userSettings.recentColors shouldBe listOf(PRESET_COLOR, OTHER_COLOR)
        }
    }

    @Test
    fun colorButton_showsTheColorPickerDialog() {
        withFragment {
            click(R.id.colorButton)
            ShadowDialog.getLatestDialog().isShowing shouldBe true
        }
    }

    @Test
    fun colorDialog_activityDestroyedBeforeThePostedBitmapListenerRuns_doesNotCrashAndShowsNoDialog() {
        val controller = Robolectric.buildActivity(MainActivity::class.java, Intent(context, MainActivity::class.java)).setup()
        shadowOf(Looper.getMainLooper()).idle()
        val host = controller.get().supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
        host.navController.navigate(R.id.picker_dest)
        shadowOf(Looper.getMainLooper()).idle()
        val fragment = host.childFragmentManager.primaryNavigationFragment as TabPickerFragment
        fragment.requireView().findViewById<View>(R.id.colorButton).performClick()
        controller.pause().stop().destroy()
        fragment.activity shouldBe null
        shadowOf(Looper.getMainLooper()).idle()
        latestDialog().isShowing shouldBe false
    }

    @Test
    fun configurationChanged_withoutDialog_opensNoDialog() {
        withFragmentController { controller, _ ->
            controller.changeConfiguration()
            ShadowDialog.getLatestDialog() shouldBe null
        }
    }

    @Test
    fun configurationChanged_withDismissedDialog_doesNotReopen() {
        withFragmentController { controller, fragment ->
            fragment.click(R.id.colorButton)
            val dialog = latestDialog()
            dialog.dismiss()
            controller.changeConfiguration()
            ShadowDialog.getLatestDialog() shouldBeSameInstanceAs dialog
            dialog.isShowing shouldBe false
        }
    }

    @Test
    fun configurationChanged_withShowingDialog_dismissesAndReopens() {
        withFragmentController { controller, fragment ->
            fragment.click(R.id.colorButton)
            val dialog = latestDialog()
            controller.changeConfiguration()
            dialog.isShowing shouldBe false
            ShadowDialog.getLatestDialog() shouldNotBeSameInstanceAs dialog
            ShadowDialog.getLatestDialog().isShowing shouldBe true
        }
    }

    @Test
    fun numberPicker1_next_movesEditingToPicker2() {
        withFragment {
            numberPicker(R.id.numberPicker1).editText.onEditorAction(EditorInfo.IME_ACTION_NEXT)
            numberPicker(R.id.numberPicker1).isEditTextMode shouldBe false
            numberPicker(R.id.numberPicker2).isEditTextMode shouldBe true
        }
    }

    @Test
    fun numberPicker1_otherAction_keepsPicker2Unchanged() {
        withFragment {
            numberPicker(R.id.numberPicker1).editText.onEditorAction(EditorInfo.IME_ACTION_GO)
            numberPicker(R.id.numberPicker2).isEditTextMode shouldBe false
        }
    }

    @Test
    fun numberPicker2_next_movesEditingToPicker3() {
        withFragment {
            numberPicker(R.id.numberPicker2).editText.onEditorAction(EditorInfo.IME_ACTION_NEXT)
            numberPicker(R.id.numberPicker2).isEditTextMode shouldBe false
            numberPicker(R.id.numberPicker3).isEditTextMode shouldBe true
        }
    }

    @Test
    fun numberPicker2_otherAction_keepsPicker3Unchanged() {
        withFragment {
            numberPicker(R.id.numberPicker2).editText.onEditorAction(EditorInfo.IME_ACTION_GO)
            numberPicker(R.id.numberPicker3).isEditTextMode shouldBe false
        }
    }

    @Test
    fun numberPicker3_done_leavesEditMode() {
        withFragment {
            numberPicker(R.id.numberPicker3).isEditTextMode = true
            numberPicker(R.id.numberPicker3).editText.onEditorAction(EditorInfo.IME_ACTION_DONE)
            numberPicker(R.id.numberPicker3).isEditTextMode shouldBe false
        }
    }

    @Test
    fun numberPicker3_otherAction_keepsEditMode() {
        withFragment {
            numberPicker(R.id.numberPicker3).isEditTextMode = true
            numberPicker(R.id.numberPicker3).editText.onEditorAction(EditorInfo.IME_ACTION_GO)
            numberPicker(R.id.numberPicker3).isEditTextMode shouldBe true
        }
    }

    private fun TabPickerFragment.selectPicker(position: Int) {
        requireView().findViewById<Spinner>(R.id.pickerSpinner).setSelection(position)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun TabPickerFragment.visiblePickers(): List<Int> =
        listOf(R.id.numberPicker, R.id.timePicker, R.id.datePicker, R.id.spinningDatePicker, R.id.sleepPicker)
            .filter { requireView().findViewById<View>(it).isVisible }

    @Test
    fun pickerSpinner_nothingSelected_keepsTheVisiblePicker() {
        withFragment {
            val spinner = requireView().findViewById<Spinner>(R.id.pickerSpinner)
            spinner.onItemSelectedListener.shouldNotBeNull().onNothingSelected(spinner)
            visiblePickers() shouldBe listOf(R.id.numberPicker)
        }
    }

    @Test
    fun pickerSpinner_selection_showsOnlyTheMatchingPicker() {
        withFragment {
            selectPicker(1)
            visiblePickers() shouldBe listOf(R.id.timePicker)
            selectPicker(2)
            visiblePickers() shouldBe listOf(R.id.datePicker)
            selectPicker(3)
            visiblePickers() shouldBe listOf(R.id.spinningDatePicker)
            selectPicker(4)
            visiblePickers() shouldBe listOf(R.id.sleepPicker)
            selectPicker(0)
            visiblePickers() shouldBe listOf(R.id.numberPicker)
        }
    }

    companion object {
        private const val PRESET_COLOR = 0xFF123456.toInt()
        private const val OTHER_COLOR = 0xFF654321.toInt()
    }
}
