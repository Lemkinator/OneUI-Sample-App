package dev.oneuiproject.oneui.oneuisampleapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Provides CRUD operations for user settings. */
class UserSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** Returns the current user settings. */
    suspend fun getSettings(): UserSettings = dataStore.data.map(::settingsFromPreferences).first()

    /**
     * Updates the current user settings and returns the new settings.
     * @param f Invoked with the current settings; The settings returned from this function will replace the current ones.
     */
    suspend fun updateSettings(f: (UserSettings) -> UserSettings): UserSettings {
        val prefs = dataStore.edit {
            val newSettings = f(settingsFromPreferences(it))
            it[KEY_LAST_VERSION_CODE] = newSettings.lastVersionCode
            it[KEY_LAST_VERSION_NAME] = newSettings.lastVersionName
            it[KEY_DARK_MODE] = newSettings.darkMode
            it[KEY_AUTO_DARK_MODE] = newSettings.autoDarkMode
            it[KEY_TOS_ACCEPTED] = newSettings.tosAccepted
            it[KEY_DEV_MODE_ENABLED] = newSettings.devModeEnabled
            it[KEY_SEARCH] = newSettings.search
            it[KEY_CURRENT_FRAGMENT] = newSettings.currentFragment
            it[KEY_SHOW_LETTERS] = newSettings.showLetters
            it[KEY_SHOW_SYSTEM_APPS] = newSettings.showSystemApps
            it[KEY_SAMPLE_SWITCHBAR] = newSettings.sampleSwitchbar
        }
        return settingsFromPreferences(prefs)
    }


    private fun settingsFromPreferences(prefs: Preferences) = UserSettings(
        lastVersionCode = prefs[KEY_LAST_VERSION_CODE] ?: -1,
        lastVersionName = prefs[KEY_LAST_VERSION_NAME] ?: "0.0",
        darkMode = prefs[KEY_DARK_MODE] == true,
        autoDarkMode = prefs[KEY_AUTO_DARK_MODE] != false,
        tosAccepted = prefs[KEY_TOS_ACCEPTED] == true,
        devModeEnabled = prefs[KEY_DEV_MODE_ENABLED] == true,
        search = prefs[KEY_SEARCH] ?: "",
        currentFragment = prefs[KEY_CURRENT_FRAGMENT] ?: 0,
        showLetters = prefs[KEY_SHOW_LETTERS] != false,
        showSystemApps = prefs[KEY_SHOW_SYSTEM_APPS] == true,
        sampleSwitchbar = prefs[KEY_SAMPLE_SWITCHBAR] == true,
    )


    private companion object {
        private val KEY_LAST_VERSION_CODE = intPreferencesKey("lastVersionCode")
        private val KEY_LAST_VERSION_NAME = stringPreferencesKey("lastVersionName")
        private val KEY_DARK_MODE = booleanPreferencesKey("darkMode")
        private val KEY_AUTO_DARK_MODE = booleanPreferencesKey("autoDarkMode")
        private val KEY_TOS_ACCEPTED = booleanPreferencesKey("tosAccepted")
        private val KEY_DEV_MODE_ENABLED = booleanPreferencesKey("devModeEnabled")
        private val KEY_SEARCH = stringPreferencesKey("search")
        private val KEY_CURRENT_FRAGMENT = intPreferencesKey("currentFragment")
        private val KEY_SHOW_LETTERS = booleanPreferencesKey("showLetters")
        private val KEY_SHOW_SYSTEM_APPS = booleanPreferencesKey("showSystemApps")
        private val KEY_SAMPLE_SWITCHBAR = booleanPreferencesKey("sampleSwitchbar")
    }
}

/** Settings associated with the current user. */
data class UserSettings(
    /** Last App-Version-Code */
    val lastVersionCode: Int,
    /** Last App-Version-Name */
    val lastVersionName: String,
    /** Dark Mode enabled */
    val darkMode: Boolean,
    /** Auto Dark Mode enabled */
    val autoDarkMode: Boolean,
    /** terms of service accepted by user */
    val tosAccepted: Boolean,
    /** devMode enabled */
    val devModeEnabled: Boolean,
    /** current search */
    val search: String,
    /** current Fragment*/
    val currentFragment: Int,
    /** show letters*/
    val showLetters: Boolean,
    /** show system apps*/
    val showSystemApps: Boolean,
    /** sample switchbar*/
    val sampleSwitchbar: Boolean,
)
