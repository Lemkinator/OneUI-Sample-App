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
    val devModeEnabled: Boolean = false,
    val sampleSwitchBar: Boolean = false,
)

/**
 * Only [devModeEnabled] and [sampleSwitchBar] are observed here: both can change while this screen is alive but
 * paused (devModeEnabled from [AboutViewModel]'s hidden toggle; sampleSwitchBar from [SwitchBarViewModel] when the
 * user backs out of [de.lemke.oneuisample.ui.SwitchBarActivity]), so the widgets need a push from outside to stay
 * current. darkMode/autoDarkMode have no such external writer - their widgets' own native persistence is the only
 * source of truth, so this ViewModel doesn't need to know about them at all.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    userSettings: UserSettings,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> =
        userSettings.flow
            .map { SettingsUiState(it.devModeEnabled, it.sampleSwitchBar) }
            .stateInViewModel(
                viewModelScope,
                SettingsUiState(
                    devModeEnabled = userSettings.devModeEnabled,
                    sampleSwitchBar = userSettings.sampleSwitchBar,
                ),
            )
}
