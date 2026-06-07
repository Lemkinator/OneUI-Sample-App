package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SwitchBarUiState(
    val enabled: Boolean = false,
)

@HiltViewModel
class SwitchBarViewModel @Inject constructor(
    private val userSettings: UserSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SwitchBarUiState(enabled = userSettings.sampleSwitchBar))
    val state: StateFlow<SwitchBarUiState> = _state.asStateFlow()

    fun onSwitchChanged(enabled: Boolean) {
        userSettings.sampleSwitchBar = enabled
        _state.update { it.copy(enabled = enabled) }
    }
}
