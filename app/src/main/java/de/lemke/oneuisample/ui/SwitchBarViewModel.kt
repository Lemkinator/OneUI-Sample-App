package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.util.stateInViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

data class SwitchBarUiState(
    val enabled: Boolean = false,
)

@HiltViewModel
class SwitchBarViewModel @Inject constructor(
    private val userSettings: UserSettingsRepository,
) : ViewModel() {
    val state: StateFlow<SwitchBarUiState> =
        userSettings.flow
            .map { SwitchBarUiState(enabled = it.sampleSwitchBar) }
            .stateInViewModel(viewModelScope, SwitchBarUiState(enabled = userSettings.sampleSwitchBar))

    fun onSwitchChanged(enabled: Boolean) {
        userSettings.sampleSwitchBar = enabled
    }
}
