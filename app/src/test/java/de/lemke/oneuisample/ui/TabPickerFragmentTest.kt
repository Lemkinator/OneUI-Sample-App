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
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.NavHostFragment
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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

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

    @Test
    fun onDatePicked_showsSnackBar() {
        withFragment { onDatePicked(2025, 11, 25) }
    }

    @Test
    fun onTimePicked_showsSnackBar() {
        withFragment { onTimePicked(14, 30) }
    }

    @Test
    fun onStartEndTimePicked_showsSnackBar() {
        withFragment { onStartEndTimePicked(480, 1020) }
    }

    @Test
    fun onColorPicked_updatesCurrentColor() {
        withFragment {
            onColorPicked(0xFF0000)
            userSettings.currentColor shouldBe 0xFF0000
        }
    }

    @Test
    fun onColorPicked_deduplicatesRecentColors() {
        withFragment {
            onColorPicked(0xFF0000)
            onColorPicked(0xFF0000)
            // Picked color deduped: list stays at 2 (0xFF0000 + initial default), not 3
            userSettings.recentColors.count { it == 0xFF0000 } shouldBe 1
        }
    }

    @Test
    fun onColorPicked_keepsAtMostSixRecentColors() {
        withFragment {
            repeat(8) { i -> onColorPicked(i) }
            userSettings.recentColors.size shouldBe 6
        }
    }

    @Test
    fun openDatePickerDialog_showsDialog() {
        withFragment { openDatePickerDialog() }
        ShadowDialog.getLatestDialog() shouldNotBe null
    }

    @Test
    fun openTimePickerDialog_showsDialog() {
        withFragment { openTimePickerDialog() }
        ShadowDialog.getLatestDialog() shouldNotBe null
    }

    @Test
    fun openStartEndTimePickerDialog_showsDialog() {
        withFragment { openStartEndTimePickerDialog() }
        ShadowDialog.getLatestDialog() shouldNotBe null
    }

    @Test
    fun openColorPickerDialog_showsDialog() {
        withFragment {
            openColorPickerDialog()
            colorPickerDialog shouldNotBe null
        }
    }

    @Test
    fun onConfigurationChanged_withNoDialog_doesNothing() {
        withFragment {
            val config = Configuration(resources.configuration)
            onConfigurationChanged(config)
        }
    }

    @Test
    fun onConfigurationChanged_withDismissedDialog_doesNotReopen() {
        withFragment {
            openColorPickerDialog()
            shadowOf(Looper.getMainLooper()).idle()
            colorPickerDialog?.dismiss()
            val config = Configuration(resources.configuration)
            onConfigurationChanged(config)
            colorPickerDialog?.isShowing shouldBe false
        }
    }

    @Test
    fun onNumberPicker3EditorAction_done_disablesEditTextMode() {
        withFragment { onNumberPicker3EditorAction(EditorInfo.IME_ACTION_DONE) shouldBe false }
    }

    @Test
    fun onNumberPicker3EditorAction_other_noChange() {
        withFragment { onNumberPicker3EditorAction(EditorInfo.IME_ACTION_GO) shouldBe false }
    }

    @Test
    fun onNumberPicker2EditorAction_next_movesToPicker3() {
        withFragment { onNumberPicker2EditorAction(EditorInfo.IME_ACTION_NEXT) shouldBe false }
    }

    @Test
    fun onNumberPicker2EditorAction_other_noChange() {
        withFragment { onNumberPicker2EditorAction(EditorInfo.IME_ACTION_GO) shouldBe false }
    }

    @Test
    fun onNumberPicker1EditorAction_next_movesToPicker2() {
        withFragment { onNumberPicker1EditorAction(EditorInfo.IME_ACTION_NEXT) shouldBe false }
    }

    @Test
    fun onNumberPicker1EditorAction_other_noChange() {
        withFragment { onNumberPicker1EditorAction(EditorInfo.IME_ACTION_GO) shouldBe false }
    }

    @Test
    fun onSpinnerItemSelected_null_doesNothing() {
        withFragment { onSpinnerItemSelected(null) }
    }

    @Test
    fun onSpinnerItemSelected_position0_showsNumberPicker() {
        withFragment {
            onSpinnerItemSelected(0)
            requireView().findViewById<View>(R.id.numberPicker)?.visibility shouldBe View.VISIBLE
            requireView().findViewById<View>(R.id.timePicker)?.visibility shouldBe View.GONE
        }
    }

    @Test
    fun onSpinnerItemSelected_position1_showsTimePicker() {
        withFragment {
            onSpinnerItemSelected(1)
            requireView().findViewById<View>(R.id.timePicker)?.visibility shouldBe View.VISIBLE
            requireView().findViewById<View>(R.id.numberPicker)?.visibility shouldBe View.GONE
        }
    }

    @Test
    fun onSpinnerItemSelected_position2_showsDatePicker() {
        withFragment {
            onSpinnerItemSelected(2)
            requireView().findViewById<View>(R.id.datePicker)?.visibility shouldBe View.VISIBLE
        }
    }

    @Test
    fun onSpinnerItemSelected_position3_showsSpinningDatePicker() {
        withFragment {
            onSpinnerItemSelected(3)
            requireView().findViewById<View>(R.id.spinningDatePicker)?.visibility shouldBe View.VISIBLE
        }
    }

    @Test
    fun onSpinnerItemSelected_position4_showsSleepPicker() {
        withFragment {
            onSpinnerItemSelected(4)
            requireView().findViewById<View>(R.id.sleepPicker)?.visibility shouldBe View.VISIBLE
        }
    }

    @Test
    fun onConfigurationChanged_withDialogShowing_dismissesAndReopens() {
        withFragment {
            openColorPickerDialog()
            val config = Configuration(resources.configuration)
            onConfigurationChanged(config)
            shadowOf(Looper.getMainLooper()).idle()
            colorPickerDialog?.isShowing shouldBe true
        }
    }

    @Test
    fun onConfigurationChanged_withDialogExistingNotShowing_doesNothing() {
        withFragment {
            openColorPickerDialog()
            colorPickerDialog?.dismiss()
            val config = Configuration(resources.configuration)
            onConfigurationChanged(config)
            colorPickerDialog?.isShowing shouldBe false
        }
    }

    @Test
    fun openStartEndTimePickerDialog_positiveButton_callsOnStartEndTimePicked() {
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
                fragment?.openStartEndTimePickerDialog()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                (ShadowDialog.getLatestDialog() as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun numberPicker3_editorAction_triggersCallback() {
        withFragment {
            requireView()
                .findViewById<SeslNumberPicker>(R.id.numberPicker3)
                ?.editText
                ?.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }
    }

    @Test
    fun numberPicker2_editorAction_triggersCallback() {
        withFragment {
            requireView()
                .findViewById<SeslNumberPicker>(R.id.numberPicker2)
                ?.editText
                ?.onEditorAction(EditorInfo.IME_ACTION_NEXT)
        }
    }

    @Test
    fun numberPicker1_editorAction_triggersCallback() {
        withFragment {
            requireView()
                .findViewById<SeslNumberPicker>(R.id.numberPicker1)
                ?.editText
                ?.onEditorAction(EditorInfo.IME_ACTION_NEXT)
        }
    }
}
