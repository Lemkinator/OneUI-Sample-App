package de.lemke.oneuisampleapp.data

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
            it[KEY_TOS_ACCEPTED] = newSettings.tosAccepted
            it[KEY_DEV_MODE_ENABLED] = newSettings.devModeEnabled
            it[KEY_CONFIRM_EXIT] = newSettings.confirmExit
            it[KEY_CURRENT_FRAGMENT] = newSettings.currentFragment
            it[KEY_SHOW_LETTERS] = newSettings.showLetters
            it[KEY_SHOW_SYSTEM_APPS] = newSettings.showSystemApps
        }
        return settingsFromPreferences(prefs)
    }


    private fun settingsFromPreferences(prefs: Preferences) = UserSettings(
        lastVersionCode = prefs[KEY_LAST_VERSION_CODE] ?: -1,
        lastVersionName = prefs[KEY_LAST_VERSION_NAME] ?: "0.0",
        tosAccepted = prefs[KEY_TOS_ACCEPTED] ?: false,
        devModeEnabled = prefs[KEY_DEV_MODE_ENABLED] ?: false,
        confirmExit = prefs[KEY_CONFIRM_EXIT] ?: true,
        currentFragment = prefs[KEY_CURRENT_FRAGMENT] ?: 0,
        showLetters = prefs[KEY_SHOW_LETTERS] ?: true,
        showSystemApps = prefs[KEY_SHOW_SYSTEM_APPS] ?: false,
    )


    private companion object {
        private val KEY_LAST_VERSION_CODE = intPreferencesKey("lastVersionCode")
        private val KEY_LAST_VERSION_NAME = stringPreferencesKey("lastVersionName")
        private val KEY_TOS_ACCEPTED = booleanPreferencesKey("tosAccepted")
        private val KEY_DEV_MODE_ENABLED = booleanPreferencesKey("devModeEnabled")
        private val KEY_CONFIRM_EXIT = booleanPreferencesKey("confirmExit")
        private val KEY_CURRENT_FRAGMENT = intPreferencesKey("currentFragment")
        private val KEY_SHOW_LETTERS = booleanPreferencesKey("showLetters")
        private val KEY_SHOW_SYSTEM_APPS = booleanPreferencesKey("showSystemApps")
    }
}

/** Settings associated with the current user. */
data class UserSettings(
    /** Last App-Version-Code */
    val lastVersionCode: Int,
    /** Last App-Version-Name */
    val lastVersionName: String,
    /** terms of service accepted by user */
    val tosAccepted: Boolean,
    /** devMode enabled */
    val devModeEnabled: Boolean,
    /** confirm Exit*/
    val confirmExit: Boolean,
    /** current Fragment*/
    val currentFragment: Int,
    /** show letters*/
    val showLetters: Boolean,
    /** show system apps*/
    val showSystemApps: Boolean,
)
