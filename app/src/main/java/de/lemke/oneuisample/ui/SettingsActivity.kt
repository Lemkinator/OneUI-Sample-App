package de.lemke.oneuisample.ui

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SeslSwitchPreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivitySettingsBinding
import de.lemke.oneuisample.ui.util.collectState
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.ktx.addRelativeLinksCard
import dev.oneuiproject.oneui.ktx.onClick
import dev.oneuiproject.oneui.ktx.onNewValue
import dev.oneuiproject.oneui.preference.HorizontalRadioPreference
import dev.oneuiproject.oneui.preference.InsetPreferenceCategory
import dev.oneuiproject.oneui.preference.SuggestionCardPreference
import dev.oneuiproject.oneui.preference.TipsCardPreference
import dev.oneuiproject.oneui.preference.UpdatableWidgetPreference
import dev.oneuiproject.oneui.widget.RelativeLink
import dev.oneuiproject.oneui.design.R as designR

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var settingsActivity: SettingsActivity
        private lateinit var darkModePref: HorizontalRadioPreference
        private lateinit var autoDarkModePref: SwitchPreferenceCompat
        private val viewModel: SettingsViewModel by viewModels()
        private lateinit var devOptionsPref: PreferenceCategory
        private lateinit var switchScreenPref: SeslSwitchPreferenceScreen

        override fun onAttach(context: Context) {
            super.onAttach(context)
            if (activity is SettingsActivity) settingsActivity = activity as SettingsActivity
        }

        override fun onCreatePreferences(
            bundle: Bundle?,
            str: String?,
        ) {
            addPreferencesFromResource(R.xml.preferences)
        }

        override fun onCreate(bundle: Bundle?) {
            super.onCreate(bundle)
            initPreferences()
        }

        override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?,
        ) {
            super.onViewCreated(view, savedInstanceState)
            requireView().setBackgroundColor(resources.getColor(designR.color.oui_des_background_color, settingsActivity.theme))
            addRelativeLinksCard(
                RelativeLink(getString(R.string.about_custom)) { startActivity(Intent(settingsActivity, CustomAboutActivity::class.java)) },
            )
            collectState(viewModel.state) { render(it) }
        }

        private fun render(state: SettingsUiState) {
            devOptionsPref.isVisible = state.devModeEnabled
            autoDarkModePref.isChecked = state.autoDarkMode
            darkModePref.isEnabled = !state.autoDarkMode
            darkModePref.value = if (state.darkMode) "1" else "0"
            switchScreenPref.apply {
                isChecked = state.sampleSwitchBar
                summary = if (isChecked) "Enabled" else "Disabled"
            }
        }

        private fun initPreferences() {
            devOptionsPref = findPreference("dev_options")!!
            switchScreenPref = findPreference("switch_screen")!!
            initDarkModePrefs()
            initSwitchBarPref()
            initLanguagePref()
            initTosPref()
            initDeleteAppDataPref()
            initSuggestionCard()
            findPreference<UpdatableWidgetPreference>("updatable")?.onClick {
                it.widgetLayoutResource = R.layout.sample_pref_widget_progress
                view?.postDelayed({ it.widgetLayoutResource = R.layout.sample_pref_widget_check }, 2000)
            }
            val tips = findPreference<TipsCardPreference>("tip")
            tips?.addButton("Button") { suggestiveSnackBar("onClick") }
            findPreference<EditTextPreference>("edit_text")?.onNewValue { suggestiveSnackBar("New value: $it") }
        }

        private fun initDarkModePrefs() {
            darkModePref = findPreference("dark_mode_pref")!!
            autoDarkModePref = findPreference("dark_mode_auto_pref")!!
            darkModePref.onNewValue { newValue ->
                val darkMode = newValue == "1"
                AppCompatDelegate.setDefaultNightMode(if (darkMode) MODE_NIGHT_YES else MODE_NIGHT_NO)
                viewModel.onDarkModeChanged(darkMode)
            }
            autoDarkModePref.onNewValue { newValue ->
                if (newValue) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(if (viewModel.state.value.darkMode) MODE_NIGHT_YES else MODE_NIGHT_NO)
                }
                viewModel.onAutoDarkModeChanged(newValue)
            }
            darkModePref.setDividerEnabled(false)
            darkModePref.setTouchEffectEnabled(false)
        }

        private fun initSwitchBarPref() {
            switchScreenPref.apply {
                onClick { startActivity(Intent(settingsActivity, SwitchBarActivity::class.java)) }
                onNewValue { newValue ->
                    summary = if (newValue) "Enabled" else "Disabled"
                    viewModel.onSampleSwitchBarChanged(newValue)
                }
            }
        }

        private fun initLanguagePref() {
            if (SDK_INT >= TIRAMISU) {
                findPreference<PreferenceCategory>("language_pref_cat")!!.isVisible = true
                findPreference<PreferenceScreen>("language_pref")?.onClick {
                    try {
                        startActivity(Intent(Settings.ACTION_APP_LOCALE_SETTINGS, "package:${settingsActivity.packageName}".toUri()))
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        suggestiveSnackBar(getString(R.string.change_language_not_supported_by_device))
                    }
                }
            }
        }

        private fun initTosPref() {
            findPreference<PreferenceScreen>("tos_pref")?.onClick {
                AlertDialog
                    .Builder(requireContext())
                    .setTitle(getString(R.string.tos))
                    .setMessage(getString(R.string.tos_content))
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    .show()
            }
        }

        private fun initDeleteAppDataPref() {
            findPreference<PreferenceScreen>("delete_app_data_pref")?.onClick {
                AlertDialog
                    .Builder(settingsActivity)
                    .setTitle(R.string.delete_appdata_and_exit)
                    .setMessage(R.string.delete_appdata_and_exit_warning)
                    .setNegativeButton(designR.string.oui_des_common_cancel, null)
                    .setPositiveButton(designR.string.oui_des_common_button_yes) { _: DialogInterface, _: Int ->
                        (settingsActivity.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    }.show()
            }
        }

        private fun initSuggestionCard() {
            val suggestion = findPreference<SuggestionCardPreference>("suggestion")!!
            val suggestionInset = findPreference<InsetPreferenceCategory>("suggestion_inset")!!
            suggestion.setOnClosedClickedListener { preferenceScreen.removePreference(suggestionInset) }
            suggestion.setActionButtonOnClickListener {
                suggestion.startTurnOnAnimation("Turned on")
                it.postDelayed(
                    {
                        preferenceScreen.removePreference(suggestion)
                        preferenceScreen.removePreference(suggestionInset)
                    },
                    1500,
                )
            }
        }
    }
}
