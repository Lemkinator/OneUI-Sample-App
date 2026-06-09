package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
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
    private val userSettings: UserSettingsRepository,
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
