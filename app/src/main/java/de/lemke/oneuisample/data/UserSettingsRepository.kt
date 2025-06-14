package de.lemke.oneuisample.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.oneuiproject.oneui.layout.ToolbarLayout
import de.lemke.oneuisample.data.SearchOnActionMode.Companion.DEFAULT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Provides CRUD operations for user settings. */
class UserSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** Returns the current user settings. */
    suspend fun getUserSettings(): UserSettings = dataStore.data.map(::settingsFromPreferences).first()

    /** Returns a user settings flow. */
    fun observeUserSettings() = dataStore.data.map(::settingsFromPreferences)

    /** Emits the current showSystemApps setting. */
    fun observeShowSystemApps(): Flow<Boolean> = dataStore.data.map { it[KEY_SHOW_SYSTEM_APPS] == true }.distinctUntilChanged()

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
            it[KEY_SEARCH_ACTIVE] = newSettings.searchActive
            it[KEY_SHOW_SYSTEM_APPS] = newSettings.showSystemApps
            it[KEY_APP_PICKER_TYPE] = newSettings.appPickerType
            it[KEY_SAMPLE_SWITCH_BAR] = newSettings.sampleSwitchBar
            it[KEY_SHOW_INDEX_SCROLL] = newSettings.showIndexScroll
            it[KEY_INDEX_SCROLL_SHOW_LETTERS] = newSettings.indexScrollShowLetters
            it[KEY_INDEX_SCROLL_AUTO_HIDE] = newSettings.indexScrollAutoHide
            it[KEY_ACTION_MODE_SHOW_CANCEL] = newSettings.actionModeShowCancel
            it[KEY_ACTION_MODE_KEEP_SEARCH] = newSettings.searchOnActionMode.ordinal
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
        searchActive = prefs[KEY_SEARCH_ACTIVE] == true,
        showSystemApps = prefs[KEY_SHOW_SYSTEM_APPS] == true,
        appPickerType = prefs[KEY_APP_PICKER_TYPE] ?: 0,
        sampleSwitchBar = prefs[KEY_SAMPLE_SWITCH_BAR] == true,
        showIndexScroll = prefs[KEY_SHOW_INDEX_SCROLL] != false,
        indexScrollShowLetters = prefs[KEY_INDEX_SCROLL_SHOW_LETTERS] != false,
        indexScrollAutoHide = prefs[KEY_INDEX_SCROLL_AUTO_HIDE] != false,
        actionModeShowCancel = prefs[KEY_ACTION_MODE_SHOW_CANCEL] == true,
        searchOnActionMode = prefs[KEY_ACTION_MODE_KEEP_SEARCH]?.let { SearchOnActionMode.entries.getOrNull(it) } ?: DEFAULT,
    )


    private companion object {
        private val KEY_LAST_VERSION_CODE = intPreferencesKey("lastVersionCode")
        private val KEY_LAST_VERSION_NAME = stringPreferencesKey("lastVersionName")
        private val KEY_DARK_MODE = booleanPreferencesKey("darkMode")
        private val KEY_AUTO_DARK_MODE = booleanPreferencesKey("autoDarkMode")
        private val KEY_TOS_ACCEPTED = booleanPreferencesKey("tosAccepted")
        private val KEY_DEV_MODE_ENABLED = booleanPreferencesKey("devModeEnabled")
        private val KEY_SEARCH = stringPreferencesKey("search")
        private val KEY_SEARCH_ACTIVE = booleanPreferencesKey("searchActive")
        private val KEY_SHOW_SYSTEM_APPS = booleanPreferencesKey("showSystemApps")
        private val KEY_APP_PICKER_TYPE = intPreferencesKey("appPickerType")
        private val KEY_SAMPLE_SWITCH_BAR = booleanPreferencesKey("sampleSwitchBar")
        private val KEY_SHOW_INDEX_SCROLL = booleanPreferencesKey("showIndexScroll")
        private val KEY_INDEX_SCROLL_SHOW_LETTERS = booleanPreferencesKey("indexScrollShowLetters")
        private val KEY_INDEX_SCROLL_AUTO_HIDE = booleanPreferencesKey("indexScrollAutoHide")
        private val KEY_ACTION_MODE_SHOW_CANCEL = booleanPreferencesKey("actionModeShowCancel")
        private val KEY_ACTION_MODE_KEEP_SEARCH = intPreferencesKey("actionModeKeepSearch")
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
    /** search active */
    val searchActive: Boolean,
    /** show system apps*/
    val showSystemApps: Boolean,
    /** app picker type*/
    val appPickerType: Int,
    /** sample switchBar*/
    val sampleSwitchBar: Boolean,
    /** show indexScroll*/
    val showIndexScroll: Boolean,
    /** show letters in indexScroll*/
    val indexScrollShowLetters: Boolean,
    /** auto hide indexScroll*/
    val indexScrollAutoHide: Boolean,
    /** show cancel button in action mode*/
    val actionModeShowCancel: Boolean,
    /** search on action mode*/
    val searchOnActionMode: SearchOnActionMode,
)

enum class SearchOnActionMode {
    DISMISS,
    NO_DISMISS,
    CONCURRENT;

    companion object {
        val DEFAULT = DISMISS
    }

    fun getToolbarValue(listener: ToolbarLayout.SearchModeListener?) = when (this) {
        DISMISS -> ToolbarLayout.SearchOnActionMode.Dismiss
        NO_DISMISS -> ToolbarLayout.SearchOnActionMode.NoDismiss
        CONCURRENT -> ToolbarLayout.SearchOnActionMode.Concurrent(listener)
    }
}
