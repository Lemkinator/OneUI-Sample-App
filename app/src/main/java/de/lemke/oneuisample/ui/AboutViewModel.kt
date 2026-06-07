package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.util.stateInViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

data class AboutUiState(
    val devModeEnabled: Boolean = false,
)

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val userSettings: UserSettingsRepository,
) : ViewModel() {
    val state: StateFlow<AboutUiState> =
        userSettings.flow
            .map { AboutUiState(devModeEnabled = it.devModeEnabled) }
            .stateInViewModel(viewModelScope, AboutUiState(devModeEnabled = userSettings.devModeEnabled))

    fun onToggleDevMode() {
        userSettings.update { copy(devModeEnabled = !devModeEnabled) }
    }
}
