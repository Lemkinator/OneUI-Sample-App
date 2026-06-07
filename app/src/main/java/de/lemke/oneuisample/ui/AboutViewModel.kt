package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AboutUiState(
    val devModeEnabled: Boolean = false,
)

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val userSettings: UserSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AboutUiState(devModeEnabled = userSettings.devModeEnabled))
    val state: StateFlow<AboutUiState> = _state.asStateFlow()

    fun onToggleDevMode() {
        val newValue = !userSettings.devModeEnabled
        userSettings.devModeEnabled = newValue
        _state.update { it.copy(devModeEnabled = newValue) }
    }
}
