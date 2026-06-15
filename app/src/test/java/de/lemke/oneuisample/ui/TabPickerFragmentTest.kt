package de.lemke.oneuisample.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Looper
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.fragments.TabPickerFragment
import org.junit.Assert.assertEquals
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
class TabPickerFragmentTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
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
        withFragment { onColorPicked(0xFF0000) }
    }

    @Test
    fun onColorPicked_deduplicatesRecentColors() {
        withFragment {
            onColorPicked(0xFF0000)
            onColorPicked(0xFF0000)
        }
    }

    @Test
    fun onColorPicked_keepsAtMostSixRecentColors() {
        withFragment {
            repeat(8) { i -> onColorPicked(i) }
        }
    }

    @Test
    fun openDatePickerDialog_showsDialog() {
        withFragment { openDatePickerDialog() }
    }

    @Test
    fun openTimePickerDialog_showsDialog() {
        withFragment { openTimePickerDialog() }
    }

    @Test
    fun openStartEndTimePickerDialog_showsDialog() {
        withFragment { openStartEndTimePickerDialog() }
    }

    @Test
    fun openColorPickerDialog_showsDialog() {
        withFragment { openColorPickerDialog() }
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
            val config = Configuration(resources.configuration)
            onConfigurationChanged(config)
        }
    }

    @Test
    fun onNumberPicker3EditorAction_done_disablesEditTextMode() {
        withFragment { onNumberPicker3EditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_DONE) }
    }

    @Test
    fun onNumberPicker3EditorAction_other_noChange() {
        withFragment { onNumberPicker3EditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_GO) }
    }

    @Test
    fun onNumberPicker2EditorAction_next_movesToPicker3() {
        withFragment { onNumberPicker2EditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) }
    }

    @Test
    fun onNumberPicker2EditorAction_other_noChange() {
        withFragment { onNumberPicker2EditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_GO) }
    }

    @Test
    fun onNumberPicker1EditorAction_next_movesToPicker2() {
        withFragment { onNumberPicker1EditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) }
    }

    @Test
    fun onNumberPicker1EditorAction_other_noChange() {
        withFragment { onNumberPicker1EditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_GO) }
    }

    @Test
    fun onSpinnerItemSelected_null_doesNothing() {
        withFragment { onSpinnerItemSelected(null) }
    }

    @Test
    fun onSpinnerItemSelected_position0_showsNumberPicker() {
        withFragment { onSpinnerItemSelected(0) }
    }

    @Test
    fun onSpinnerItemSelected_position1_showsTimePicker() {
        withFragment { onSpinnerItemSelected(1) }
    }

    @Test
    fun onSpinnerItemSelected_position2_showsDatePicker() {
        withFragment { onSpinnerItemSelected(2) }
    }

    @Test
    fun onSpinnerItemSelected_position3_showsSpinningDatePicker() {
        withFragment { onSpinnerItemSelected(3) }
    }

    @Test
    fun onSpinnerItemSelected_position4_showsSleepPicker() {
        withFragment { onSpinnerItemSelected(4) }
    }

    @Test
    fun onConfigurationChanged_withDialogShowing_dismissesAndReopens() {
        withFragment {
            openColorPickerDialog()
            val config = Configuration(resources.configuration)
            onConfigurationChanged(config)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onConfigurationChanged_withDialogExistingNotShowing_doesNothing() {
        withFragment {
            openColorPickerDialog()
            colorPickerDialog?.dismiss()
            val config = Configuration(resources.configuration)
            onConfigurationChanged(config)
        }
    }
}
