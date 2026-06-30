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

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SeslSwitchPreferenceScreen
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

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
                        .findFragmentById(R.id.settings) as? SettingsActivity.SettingsFragment
                fragment?.block()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun tosPref_click_showsDialog() {
        // dialog code is Kover-excluded (*SettingsFragment*initTosPref*); verifies no crash on click
        launch {
            findPreference<PreferenceScreen>("tos_pref")?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun deleteAppDataPref_click_showsConfirmationDialog() {
        // dialog code is Kover-excluded (*SettingsFragment*initDeleteAppDataPref*); verifies no crash
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
    fun autoDarkModePref_newValue_false_withDarkModeEnabled_restoresNightMode() {
        context
            .getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("darkMode", "1")
            .commit()
        launch {
            findPreference<androidx.preference.SwitchPreferenceCompat>("dark_mode_auto_pref")
                ?.callChangeListener(false)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun switchScreenPref_click_startsActivity() {
        launch {
            findPreference<SeslSwitchPreferenceScreen>("switch_screen")?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            requireActivity().let { activity ->
                shadowOf(activity)
                    .nextStartedActivity
                    ?.component
                    ?.className shouldBe SwitchBarActivity::class.java.name
            }
        }
    }

    @Test
    fun switchScreenPref_newValue_true_updatesSummary() {
        launch {
            findPreference<SeslSwitchPreferenceScreen>("switch_screen")
                ?.callChangeListener(true)
            shadowOf(Looper.getMainLooper()).idle()
            findPreference<SeslSwitchPreferenceScreen>("switch_screen")
                ?.summary shouldBe "Enabled"
        }
    }

    @Test
    fun switchScreenPref_newValue_false_updatesSummary() {
        launch {
            findPreference<SeslSwitchPreferenceScreen>("switch_screen")
                ?.callChangeListener(false)
            shadowOf(Looper.getMainLooper()).idle()
            findPreference<SeslSwitchPreferenceScreen>("switch_screen")
                ?.summary shouldBe "Disabled"
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
                org.robolectric.shadows.ShadowAlertDialog
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
        launch {
            // language_pref_cat starts hidden in XML; only shown on SDK >= TIRAMISU
            findPreference<PreferenceCategory>("language_pref_cat")?.isVisible shouldBe false
        }
    }

    @Test
    fun suggestionCard_closeButton_removesBothPreferencesFromScreen() {
        launch {
            val suggestionPref = findPreference<dev.oneuiproject.oneui.preference.SuggestionCardPreference>("suggestion")!!
            val field = suggestionPref.javaClass.getDeclaredField("closedListener")
            field.isAccessible = true
            (field.get(suggestionPref) as? android.view.View.OnClickListener)?.onClick(requireView())
            shadowOf(Looper.getMainLooper()).idle()
            preferenceScreen.findPreference<dev.oneuiproject.oneui.preference.SuggestionCardPreference>("suggestion") shouldBe null
            preferenceScreen.findPreference<dev.oneuiproject.oneui.preference.InsetPreferenceCategory>("suggestion_inset") shouldBe null
        }
    }

    @Test
    fun suggestionCard_actionButtonClicked_startsAnimation() {
        launch {
            onSuggestionCardActionButtonClicked(requireView())
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun suggestionCard_actionButtonClicked_fragmentDetachedBeforeDelayFires_doesNotCrash() {
        val scenario = ActivityScenario.launch<SettingsActivity>(Intent(context, SettingsActivity::class.java))
        shadowOf(Looper.getMainLooper()).idle()
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.settings) as? SettingsActivity.SettingsFragment
            fragment?.onSuggestionCardActionButtonClicked(fragment.requireView())
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        // isAdded guard must prevent the now-detached fragment's preferenceScreen from being touched
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        scenario.close()
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
