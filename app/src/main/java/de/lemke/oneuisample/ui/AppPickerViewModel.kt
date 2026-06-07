package de.lemke.oneuisample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.util.stateInViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

data class AppPickerUiState(
    val pickerType: Int = 0,
)

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    private val userSettings: UserSettingsRepository,
) : ViewModel() {
    val state: StateFlow<AppPickerUiState> =
        userSettings.flow
            .map { AppPickerUiState(pickerType = it.appPickerType) }
            .stateInViewModel(viewModelScope, AppPickerUiState(pickerType = userSettings.appPickerType))

    fun onPickerTypeChanged(type: Int) {
        userSettings.appPickerType = type
    }
}
