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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.util.stateInViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

data class SettingsUiState(
    val darkMode: Boolean = false,
    val autoDarkMode: Boolean = true,
    val devModeEnabled: Boolean = false,
    val sampleSwitchBar: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettings: UserSettings,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> =
        userSettings.flow
            .map { SettingsUiState(it.darkMode, it.autoDarkMode, it.devModeEnabled, it.sampleSwitchBar) }
            .stateInViewModel(
                viewModelScope,
                SettingsUiState(
                    darkMode = userSettings.darkMode,
                    autoDarkMode = userSettings.autoDarkMode,
                    devModeEnabled = userSettings.devModeEnabled,
                    sampleSwitchBar = userSettings.sampleSwitchBar,
                ),
            )

    fun onDarkModeChanged(darkMode: Boolean) {
        userSettings.darkMode = darkMode
    }

    fun onAutoDarkModeChanged(autoDarkMode: Boolean) {
        userSettings.autoDarkMode = autoDarkMode
    }

    fun onSampleSwitchBarChanged(enabled: Boolean) {
        userSettings.sampleSwitchBar = enabled
    }
}
