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

data class UserSettings(
    val darkMode: Boolean = false,
    val autoDarkMode: Boolean = true,
    val lastVersionCode: Int = -1,
    val lastVersionName: String = "0.0",
    val acceptedTosVersion: Int = -1,
    val devModeEnabled: Boolean = false,
    val appPickerType: Int = 0,
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
class UserSettingsRepository(
    private val preferences: SharedPreferences,
    scope: CoroutineScope,
) {
    var darkMode: Boolean by preferences.delegates.darkMode(false)
    var autoDarkMode: Boolean by preferences.delegates.boolean(true)
    var lastVersionCode: Int by preferences.delegates.int(-1)
    var lastVersionName: String by preferences.delegates.string("0.0")
    var acceptedTosVersion: Int by preferences.delegates.int(-1)
    var devModeEnabled: Boolean by preferences.delegates.boolean(false)
    var appPickerType: Int by preferences.delegates.int(0)
    var sampleSwitchBar: Boolean by preferences.delegates.boolean(false)
    var showIndexScroll: Boolean by preferences.delegates.boolean(true)
    var indexScrollShowLetters: Boolean by preferences.delegates.boolean(true)
    var indexScrollAutoHide: Boolean by preferences.delegates.boolean(true)
    var actionModeShowCancel: Boolean by preferences.delegates.boolean(false)
    var searchOnActionMode: SearchOnActionMode by preferences.delegates.searchOnActionMode()
    var search: String by preferences.delegates.string("")
    var searchActive: Boolean by preferences.delegates.boolean(false)

    private fun snapshot() =
        UserSettings(
            darkMode = darkMode,
            autoDarkMode = autoDarkMode,
            lastVersionCode = lastVersionCode,
            lastVersionName = lastVersionName,
            acceptedTosVersion = acceptedTosVersion,
            devModeEnabled = devModeEnabled,
            appPickerType = appPickerType,
            sampleSwitchBar = sampleSwitchBar,
            showIndexScroll = showIndexScroll,
            indexScrollShowLetters = indexScrollShowLetters,
            indexScrollAutoHide = indexScrollAutoHide,
            actionModeShowCancel = actionModeShowCancel,
            searchOnActionMode = searchOnActionMode,
            search = search,
            searchActive = searchActive,
        )

    /**
     * A [StateFlow] of the current [UserSettings] snapshot.
     *
     * Backed by a single, strongly-held [SharedPreferences.OnSharedPreferenceChangeListener] so the
     * listener is never GC'd. Per-field flows are available as extension properties:
     *
     * ```
     * userSettings.flow                    // StateFlow<UserSettings> (whole snapshot)
     * userSettings.flow.search             // Flow<String>
     * userSettings.flow.searchActive       // Flow<Boolean>
     *
     * combine(userSettings.flow.search,
     *         userSettings.flow.searchActive) { s, a -> ... }
     * ```
     */
    val flow: StateFlow<UserSettings> =
        callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(snapshot()) }
            preferences.registerOnSharedPreferenceChangeListener(listener)
            trySend(snapshot()) // close the gap between initial snapshot() and listener registration
            awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged()
            .stateIn(scope, SharingStarted.Eagerly, snapshot())

    /**
     * Atomically reads the current snapshot, applies [transform], and writes back only the changed
     * fields. Use for multi-field batch writes (e.g., a settings dialog applying 5 fields at once).
     * Synchronized to prevent interleaved concurrent updates.
     *
     * Single-field writes can just assign directly: `userSettings.devModeEnabled = true`.
     *
     * Usage: `userSettings.update { copy(showIndexScroll = true, indexScrollAutoHide = false) }`
     */
    @Suppress("CyclomaticComplexMethod")
    @Synchronized
    fun update(transform: UserSettings.() -> UserSettings) {
        val current = snapshot()
        val new = current.transform()
        if (new.darkMode != current.darkMode) darkMode = new.darkMode
        if (new.autoDarkMode != current.autoDarkMode) autoDarkMode = new.autoDarkMode
        if (new.lastVersionCode != current.lastVersionCode) lastVersionCode = new.lastVersionCode
        if (new.lastVersionName != current.lastVersionName) lastVersionName = new.lastVersionName
        if (new.acceptedTosVersion != current.acceptedTosVersion) acceptedTosVersion = new.acceptedTosVersion
        if (new.devModeEnabled != current.devModeEnabled) devModeEnabled = new.devModeEnabled
        if (new.appPickerType != current.appPickerType) appPickerType = new.appPickerType
        if (new.sampleSwitchBar != current.sampleSwitchBar) sampleSwitchBar = new.sampleSwitchBar
        if (new.showIndexScroll != current.showIndexScroll) showIndexScroll = new.showIndexScroll
        if (new.indexScrollShowLetters != current.indexScrollShowLetters) indexScrollShowLetters = new.indexScrollShowLetters
        if (new.indexScrollAutoHide != current.indexScrollAutoHide) indexScrollAutoHide = new.indexScrollAutoHide
        if (new.actionModeShowCancel != current.actionModeShowCancel) actionModeShowCancel = new.actionModeShowCancel
        if (new.searchOnActionMode != current.searchOnActionMode) searchOnActionMode = new.searchOnActionMode
        if (new.search != current.search) search = new.search
        if (new.searchActive != current.searchActive) searchActive = new.searchActive
    }

    companion object {
        const val PREFS_NAME = "user_settings"
    }
}

/** Per-field Flow accessors — each emits only when that field changes (distinctUntilChanged applied). **/
val StateFlow<UserSettings>.search: Flow<String> get() = map { it.search }.distinctUntilChanged()
val StateFlow<UserSettings>.searchActive: Flow<Boolean> get() = map { it.searchActive }.distinctUntilChanged()

/** Helper retrieving the [ToolbarLayout.SearchOnActionMode] from [UserSettings] with a [ToolbarLayout.SearchModeListener]. */
fun SearchOnActionMode.withListener(listener: ToolbarLayout.SearchModeListener?) =
    if (this is SearchOnActionMode.Concurrent) SearchOnActionMode.Concurrent(listener) else this
