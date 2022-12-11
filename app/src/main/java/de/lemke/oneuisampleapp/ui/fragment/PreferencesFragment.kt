package de.lemke.oneuisampleapp.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.util.SeslMisc
import androidx.preference.*
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.ui.FragmentInfo
import dev.oneuiproject.oneui.preference.HorizontalRadioPreference
import dev.oneuiproject.oneui.preference.TipsCardPreference
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard
import dev.oneuiproject.oneui.utils.PreferenceUtils
import dev.oneuiproject.oneui.widget.Toast

class PreferencesFragment : PreferenceFragmentCompat(), FragmentInfo, Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener {
    private lateinit var activityContext: Context
    private var relativeLinkCard: PreferenceRelatedCard? = null
    private var darkModePref: HorizontalRadioPreference? = null
    private var autoDarkModePref: SwitchPreferenceCompat? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        initPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().setBackgroundColor(activityContext.getColor(dev.oneuiproject.oneui.design.R.color.oui_background_color))
        listView.seslSetLastRoundedCorner(false)
    }

    override fun onResume() {
        setRelativeLinkCard()
        super.onResume()
    }

    override val layoutResId: Int = -1
    override val iconResId: Int = dev.oneuiproject.oneui.R.drawable.ic_oui_settings_outline
    override val title: CharSequence = "Preferences"
    override val isAppBarEnabled: Boolean = true

    @SuppressLint("RestrictedApi")
    private fun initPreferences() {
        val tips = findPreference<TipsCardPreference>("tip")
        tips?.addButton("Button") { Toast.makeText(activityContext, "onClick", Toast.LENGTH_SHORT).show() }

        val darkMode = AppCompatDelegate.getDefaultNightMode()
        autoDarkModePref = findPreference("dark_mode_auto")
        autoDarkModePref?.onPreferenceChangeListener = this
        autoDarkModePref?.isChecked = darkMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ||
                darkMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY ||
                darkMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        darkModePref = findPreference("dark_mode")
        darkModePref?.onPreferenceChangeListener = this
        darkModePref?.setDividerEnabled(false)
        darkModePref?.setTouchEffectEnabled(false)
        darkModePref?.isEnabled = !(autoDarkModePref?.isChecked ?: false)
        darkModePref?.value = if (SeslMisc.isLightTheme(activityContext)) "0" else "1"
        val key2 = findPreference<SeslSwitchPreferenceScreen>("key2")
        key2?.summary = if (key2?.isChecked == true) "Enabled" else "Disabled"
        key2?.onPreferenceClickListener = this
        key2?.onPreferenceChangeListener = this
        findPreference<EditTextPreference>("key4")?.onPreferenceChangeListener = this
        val key5 = findPreference<DropDownPreference>("key5")
        val key6 = findPreference<ListPreference>("key6")
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference.key == "key2") {
            Toast.makeText(activityContext, "onPreferenceClick", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    @SuppressLint("RestrictedApi")
    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            "dark_mode" -> {
                AppCompatDelegate.setDefaultNightMode(
                    if (newValue == "0") AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
                )
                return true
            }
            "dark_mode_auto" -> {
                if (newValue as Boolean) {
                    darkModePref?.isEnabled = false
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    darkModePref?.isEnabled = true
                    if (SeslMisc.isLightTheme(activityContext)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                return true
            }
            "key2" -> {
                val enabled = newValue as Boolean
                preference.summary = if (enabled) "Enabled" else "Disabled"
                return true
            }
            "key4" -> {
                val text = newValue as String
                return true
            }
        }
        return false
    }

    private fun setRelativeLinkCard() {
        if (relativeLinkCard == null) {
            relativeLinkCard = PreferenceUtils.createRelatedCard(activityContext)
            relativeLinkCard?.addButton("This", null)
                ?.addButton("That", null)
                ?.addButton("There", null)
                ?.show(this)
        }
    }
}