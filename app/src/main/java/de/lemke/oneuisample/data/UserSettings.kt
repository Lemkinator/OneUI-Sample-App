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
package de.lemke.oneuisample.data

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchOnActionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class UserSettingsSnapshot(
    val darkMode: Boolean = false,
    val autoDarkMode: Boolean = true,
    val lastVersionCode: Int = -1,
    val lastVersionName: String = "0.0",
    val acceptedTosVersion: Int = -1,
    val devModeEnabled: Boolean = false,
    val appPickerType: Int = 0,
    val appPickerSelectLayoutMode: Boolean = false,
    val sampleSwitchBar: Boolean = false,
    val showIndexScroll: Boolean = true,
    val indexScrollShowLetters: Boolean = true,
    val indexScrollAutoHide: Boolean = true,
    val actionModeShowCancel: Boolean = false,
    val searchOnActionMode: SearchOnActionMode = SearchOnActionMode.Dismiss,
    val search: String = "",
    val searchActive: Boolean = false,
)

/** SharedPreferences-backed repository for user settings. */
class UserSettings(
    private val preferences: SharedPreferences,
    scope: CoroutineScope,
) {
    /** Whether dark mode is explicitly enabled (stored as `"1"`/`"0"` for legacy `HorizontalRadioPreference` compatibility). */
    var darkMode: Boolean by preferences.delegates.darkMode(false)

    /** Whether to follow the system dark mode setting instead of the explicit [darkMode] value. */
    var autoDarkMode: Boolean by preferences.delegates.boolean(true)

    /** The version code recorded on the previous app launch, or -1 if never set. */
    var lastVersionCode: Int by preferences.delegates.int(-1)

    /** The version name recorded on the previous app launch. */
    var lastVersionName: String by preferences.delegates.string("0.0")

    /** The highest TOS version the user has accepted, or -1 if the user has never accepted. */
    var acceptedTosVersion: Int by preferences.delegates.int(-1)

    /** Whether developer mode is currently enabled. */
    var devModeEnabled: Boolean by preferences.delegates.boolean(false)

    /** The selected app picker display type. */
    var appPickerType: Int by preferences.delegates.int(0)

    /** Whether the app picker shows the dedicated selected-apps layout instead of the simple spinner-driven list. */
    var appPickerSelectLayoutMode: Boolean by preferences.delegates.boolean(false)

    /** Whether the sample switch bar is enabled. */
    var sampleSwitchBar: Boolean by preferences.delegates.boolean(false)

    /** Whether the index scroll bar is visible in list screens. */
    var showIndexScroll: Boolean by preferences.delegates.boolean(true)

    /** Whether to show letters on the index scroll bar. */
    var indexScrollShowLetters: Boolean by preferences.delegates.boolean(true)

    /** Whether the index scroll bar auto-hides after inactivity. */
    var indexScrollAutoHide: Boolean by preferences.delegates.boolean(true)

    /** Whether to show a cancel button in action mode. */
    var actionModeShowCancel: Boolean by preferences.delegates.boolean(false)

    /** The behavior of search when action mode is active. */
    var searchOnActionMode: SearchOnActionMode by preferences.delegates.searchOnActionMode()

    /** The last search query entered by the user. */
    var search: String by preferences.delegates.string("")

    /** Whether the search bar is currently active. */
    var searchActive: Boolean by preferences.delegates.boolean(false)

    /**
     * The most recently selected color in the color picker demo. Deliberately absent from
     * [UserSettingsSnapshot]/[flow] — [TabPickerFragment][de.lemke.oneuisample.ui.fragments.TabPickerFragment]
     * is the only reader, and it reads this synchronously at dialog-creation time rather than observing it.
     */
    var currentColor: Int by preferences.delegates.int(DEFAULT_COLOR)

    /**
     * The most recently used colors in the color picker demo, deduplicated and capped at [MAX_RECENT_COLORS].
     * Excluded from [UserSettingsSnapshot]/[flow] for the same reason as [currentColor].
     */
    var recentColors: List<Int> by preferences.delegates
        .intList(listOf(DEFAULT_COLOR))
        .sanitized { it.distinct().take(MAX_RECENT_COLORS) }

    /**
     * A [StateFlow] of the current [UserSettingsSnapshot].
     *
     * Backed by a single, strongly-held [SharedPreferences.OnSharedPreferenceChangeListener] so the
     * listener is never GC'd. Per-field flows are available as extension properties:
     *
     * ```
     * userSettings.flow                    // StateFlow<UserSettingsSnapshot> (whole snapshot)
     * userSettings.flow.search             // Flow<String>
     * userSettings.flow.searchActive       // Flow<Boolean>
     *
     * combine(userSettings.flow.search,
     *         userSettings.flow.searchActive) { s, a -> ... }
     * ```
     */
    val flow: StateFlow<UserSettingsSnapshot> = settingsFlow(scope, ::snapshot)

    private fun settingsFlow(
        scope: CoroutineScope,
        snapshot: () -> UserSettingsSnapshot,
    ): StateFlow<UserSettingsSnapshot> =
        callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(snapshot()) }
            preferences.registerOnSharedPreferenceChangeListener(listener)
            trySend(snapshot())
            awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged().stateIn(scope, SharingStarted.Eagerly, snapshot())

    private fun snapshot() =
        UserSettingsSnapshot(
            darkMode = darkMode,
            autoDarkMode = autoDarkMode,
            lastVersionCode = lastVersionCode,
            lastVersionName = lastVersionName,
            acceptedTosVersion = acceptedTosVersion,
            devModeEnabled = devModeEnabled,
            appPickerType = appPickerType,
            appPickerSelectLayoutMode = appPickerSelectLayoutMode,
            sampleSwitchBar = sampleSwitchBar,
            showIndexScroll = showIndexScroll,
            indexScrollShowLetters = indexScrollShowLetters,
            indexScrollAutoHide = indexScrollAutoHide,
            actionModeShowCancel = actionModeShowCancel,
            searchOnActionMode = searchOnActionMode,
            search = search,
            searchActive = searchActive,
        )

    companion object {
        const val PREFS_NAME = "user_settings"
        const val DEFAULT_COLOR = 0xFF0381FE.toInt()
        const val MAX_RECENT_COLORS = 6
    }
}

/** Per-field Flow accessors — each emits only when that field changes (distinctUntilChanged applied). **/
val StateFlow<UserSettingsSnapshot>.search: Flow<String> get() = map { it.search }.distinctUntilChanged()
val StateFlow<UserSettingsSnapshot>.searchActive: Flow<Boolean> get() = map { it.searchActive }.distinctUntilChanged()

/** Helper retrieving the [ToolbarLayout.SearchOnActionMode] from [UserSettingsSnapshot] with a [ToolbarLayout.SearchModeListener]. */
fun SearchOnActionMode.withListener(listener: ToolbarLayout.SearchModeListener?) =
    if (this is SearchOnActionMode.Concurrent) SearchOnActionMode.Concurrent(listener) else this

/** Applies this [UserSettings]'s dark mode setting to the app's default night mode. */
fun UserSettings.applyDarkMode() {
    when {
        autoDarkMode -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        darkMode -> setDefaultNightMode(MODE_NIGHT_YES)
        else -> setDefaultNightMode(MODE_NIGHT_NO)
    }
}
