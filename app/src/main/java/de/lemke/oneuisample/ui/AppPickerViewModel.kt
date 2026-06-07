package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppPickerUiState(
    val pickerType: Int = 0,
)

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    private val userSettings: UserSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AppPickerUiState(pickerType = userSettings.appPickerType))
    val state: StateFlow<AppPickerUiState> = _state.asStateFlow()

    fun onPickerTypeChanged(type: Int) {
        userSettings.appPickerType = type
        _state.update { it.copy(pickerType = type) }
    }
}
