package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SwitchBarUiState(enabled = userSettings.sampleSwitchBar),
            )

    fun onSwitchChanged(enabled: Boolean) {
        userSettings.sampleSwitchBar = enabled
    }
}
