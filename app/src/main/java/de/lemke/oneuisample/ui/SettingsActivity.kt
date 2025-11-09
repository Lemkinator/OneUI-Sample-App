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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SeslSwitchPreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivitySettingsBinding
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.ktx.addRelativeLinksCard
import dev.oneuiproject.oneui.ktx.onClick
import dev.oneuiproject.oneui.ktx.onNewValue
import dev.oneuiproject.oneui.preference.HorizontalRadioPreference
import dev.oneuiproject.oneui.preference.InsetPreferenceCategory
import dev.oneuiproject.oneui.preference.SuggestionCardPreference
import dev.oneuiproject.oneui.preference.TipsCardPreference
import dev.oneuiproject.oneui.preference.UpdatableWidgetPreference
import dev.oneuiproject.oneui.preference.app.DataStorePreferenceFragment
import dev.oneuiproject.oneui.preference.app.ObservablePreferencesDataStore
import dev.oneuiproject.oneui.widget.RelativeLink
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.oneuiproject.oneui.design.R as designR

class SampleObservablePreferencesDataStore(sampleAppPreferences: DataStore<Preferences>) :
    ObservablePreferencesDataStore(sampleAppPreferences)

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
    class SettingsFragment : DataStorePreferenceFragment() {
        private lateinit var settingsActivity: SettingsActivity
        private lateinit var darkModePref: HorizontalRadioPreference
        private lateinit var autoDarkModePref: SwitchPreferenceCompat

        @Inject
        lateinit var getUserSettings: GetUserSettingsUseCase

        @Inject
        lateinit var updateUserSettings: UpdateUserSettingsUseCase

        @Inject
        lateinit var dataStore: DataStore<Preferences>

        override fun getDataStore(): ObservablePreferencesDataStore = SampleObservablePreferencesDataStore(dataStore)

        override fun onAttach(context: Context) {
            super.onAttach(context)
            if (activity is SettingsActivity) settingsActivity = activity as SettingsActivity
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            super.onCreatePreferences(savedInstanceState, rootKey)
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            initPreferences()
        }

        private fun initPreferences() {
            darkModePref = findPreference("darkModeString")!!
            autoDarkModePref = findPreference("darkModeAuto")!!
            darkModePref.onNewValue { newValue ->
                val darkMode = newValue == "1"
                AppCompatDelegate.setDefaultNightMode(if (darkMode) MODE_NIGHT_YES else MODE_NIGHT_NO)
                lifecycleScope.launch { updateUserSettings { it.copy(darkMode = darkMode) } }
            }
            autoDarkModePref.onNewValue { newValue ->
                darkModePref.isEnabled = !newValue
                lifecycleScope.launch {
                    if (newValue) AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                    else AppCompatDelegate.setDefaultNightMode(if (getUserSettings().darkMode) MODE_NIGHT_YES else MODE_NIGHT_NO)
                }
            }
            darkModePref.setDividerEnabled(false)
            darkModePref.setTouchEffectEnabled(false)

            if (SDK_INT >= TIRAMISU) {
                findPreference<PreferenceCategory>("languagePrefCat")!!.isVisible = true
                findPreference<PreferenceScreen>("languagePref")?.onClick {
                    try {
                        startActivity(Intent(Settings.ACTION_APP_LOCALE_SETTINGS, "package:${settingsActivity.packageName}".toUri()))
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        suggestiveSnackBar(getString(R.string.change_language_not_supported_by_device))
                    }
                }
            }

            findPreference<PreferenceScreen>("tos")?.onClick {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.tos))
                    .setMessage(getString(R.string.tos_content))
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    .show()
            }
            findPreference<PreferenceScreen>("deleteAppDataPref")?.onClick {
                AlertDialog.Builder(settingsActivity)
                    .setTitle(R.string.delete_appdata_and_exit)
                    .setMessage(R.string.delete_appdata_and_exit_warning)
                    .setNegativeButton(designR.string.oui_des_common_cancel, null)
                    .setPositiveButton(designR.string.oui_des_common_button_yes) { _: DialogInterface, _: Int ->
                        (settingsActivity.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    }
                    .show()
            }

            val suggestion = findPreference<SuggestionCardPreference>("suggestion")!!
            val suggestionInset = findPreference<InsetPreferenceCategory>("suggestionInset")!!
            suggestion.setOnClosedClickedListener { preferenceScreen.removePreference(suggestionInset) }
            suggestion.setActionButtonOnClickListener {
                suggestion.startTurnOnAnimation("Turned on")
                it.postDelayed({
                    preferenceScreen.removePreference(suggestion)
                    preferenceScreen.removePreference(suggestionInset)
                }, 1500)
            }
            findPreference<UpdatableWidgetPreference>("updatable")?.onClick {
                it.widgetLayoutResource = R.layout.sample_pref_widget_progress
                view?.postDelayed({ it.widgetLayoutResource = R.layout.sample_pref_widget_check }, 2000)
            }
            val tips = findPreference<TipsCardPreference>("tip")
            tips?.addButton("Button") { suggestiveSnackBar("onClick") }
            findPreference<EditTextPreference>("editText")?.onNewValue {
                /* Place your onPreferenceChange logic here */
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            requireView().setBackgroundColor(resources.getColor(designR.color.oui_des_background_color, settingsActivity.theme))
            addRelativeLinksCard(
                RelativeLink(getString(R.string.about_custom)) { startActivity(Intent(settingsActivity, CustomAboutActivity::class.java)) }
            )
        }

        override fun onResume() {
            super.onResume()
            lifecycleScope.launch {
                val userSettings = getUserSettings()
                findPreference<PreferenceCategory>("devOptions")?.isVisible = userSettings.devModeEnabled
                darkModePref.isEnabled = !autoDarkModePref.isChecked
                darkModePref.value = if (userSettings.darkMode) "1" else "0"
                findPreference<SeslSwitchPreferenceScreen>("sampleSwitchBar")?.apply {
                    onClick { startActivity(Intent(settingsActivity, SwitchBarActivity::class.java)) }
                    summary = if (isChecked) "Enabled" else "Disabled"
                    onNewValue { newValue -> summary = if (newValue) "Enabled" else "Disabled" }
                }
            }
        }
    }
}

