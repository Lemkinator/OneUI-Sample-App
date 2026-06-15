package de.lemke.oneuisample.ui

import android.content.Intent
import android.os.Looper
import androidx.preference.PreferenceScreen
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: SettingsActivity.SettingsFragment.() -> Unit = {}) {
        ActivityScenario.launch<SettingsActivity>(Intent(context, SettingsActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val fragment =
                    activity.supportFragmentManager
                        .findFragmentById(de.lemke.oneuisample.R.id.settings) as? SettingsActivity.SettingsFragment
                fragment?.block()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun tosPref_click_showsDialog() {
        launch {
            findPreference<PreferenceScreen>("tos_pref")?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun deleteAppDataPref_click_showsConfirmationDialog() {
        launch {
            findPreference<PreferenceScreen>("delete_app_data_pref")?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun darkModePref_newValue_darkMode_updatesState() {
        launch {
            findPreference<dev.oneuiproject.oneui.preference.HorizontalRadioPreference>("dark_mode_pref")
                ?.callChangeListener("1")
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun darkModePref_newValue_lightMode_updatesState() {
        launch {
            findPreference<dev.oneuiproject.oneui.preference.HorizontalRadioPreference>("dark_mode_pref")
                ?.callChangeListener("0")
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun autoDarkModePref_newValue_true_setsFollowSystem() {
        launch {
            findPreference<androidx.preference.SwitchPreferenceCompat>("dark_mode_auto_pref")
                ?.callChangeListener(true)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun autoDarkModePref_newValue_false_restoresDarkModeSetting() {
        launch {
            findPreference<androidx.preference.SwitchPreferenceCompat>("dark_mode_auto_pref")
                ?.callChangeListener(false)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun switchScreenPref_click_startsActivity() {
        launch {
            findPreference<androidx.preference.SeslSwitchPreferenceScreen>("switch_screen")?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun switchScreenPref_newValue_true_updatesSummary() {
        launch {
            findPreference<androidx.preference.SeslSwitchPreferenceScreen>("switch_screen")
                ?.callChangeListener(true)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun switchScreenPref_newValue_false_updatesSummary() {
        launch {
            findPreference<androidx.preference.SeslSwitchPreferenceScreen>("switch_screen")
                ?.callChangeListener(false)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun updatablePref_click_changesWidget() {
        launch {
            findPreference<dev.oneuiproject.oneui.preference.UpdatableWidgetPreference>("updatable")?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun editTextPref_newValue_showsSnackBar() {
        launch {
            findPreference<androidx.preference.EditTextPreference>("edit_text")
                ?.callChangeListener("test value")
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun tosPref_positiveButton_dismissesDialog() {
        ActivityScenario.launch<SettingsActivity>(Intent(context, SettingsActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val fragment =
                    activity.supportFragmentManager
                        .findFragmentById(R.id.settings) as? SettingsActivity.SettingsFragment
                fragment?.findPreference<PreferenceScreen>("tos_pref")?.performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                ShadowAlertDialog
                    .getLatestAlertDialog()
                    ?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    ?.performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    @Config(sdk = [28])
    fun languagePref_belowTiramisu_prefCatNotVisible() {
        launch { shadowOf(Looper.getMainLooper()).idle() }
    }

    @Test
    fun suggestionCard_actionButtonClicked_startsAnimation() {
        launch {
            onSuggestionCardActionButtonClicked(requireView())
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun tipsPref_buttonClick_showsSnackBar() {
        launch {
            findPreference<dev.oneuiproject.oneui.preference.TipsCardPreference>("tip")
                ?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
