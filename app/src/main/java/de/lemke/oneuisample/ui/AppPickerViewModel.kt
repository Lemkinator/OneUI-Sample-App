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
import androidx.picker.model.AppInfoData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.di.IoDispatcher
import de.lemke.oneuisample.ui.util.AppListSource
import de.lemke.oneuisample.ui.util.ListTypes
import de.lemke.oneuisample.ui.util.stateInViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface SelectLayoutApps {
    data object Loading : SelectLayoutApps

    data class Loaded(val apps: List<AppInfoData>) : SelectLayoutApps
}

data class AppPickerUiState(
    val pickerType: Int = 0,
    val isSelectLayoutMode: Boolean = false,
    val selectLayoutApps: SelectLayoutApps = SelectLayoutApps.Loading,
)

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    private val userSettings: UserSettings,
    private val appListSource: AppListSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val selectLayoutApps = MutableStateFlow<SelectLayoutApps>(SelectLayoutApps.Loading)
    private var selectLayoutLoadStarted = false

    val state: StateFlow<AppPickerUiState> =
        combine(userSettings.flow, selectLayoutApps) { settings, apps ->
            AppPickerUiState(
                pickerType = settings.appPickerType,
                isSelectLayoutMode = settings.appPickerSelectLayoutMode,
                selectLayoutApps = apps,
            )
        }.stateInViewModel(
            viewModelScope,
            AppPickerUiState(pickerType = userSettings.appPickerType, isSelectLayoutMode = userSettings.appPickerSelectLayoutMode),
        )

    fun onSelectLayoutShown() {
        if (selectLayoutLoadStarted) return
        selectLayoutLoadStarted = true
        viewModelScope.launch {
            val apps = withContext(ioDispatcher) { appListSource.load(ListTypes.TYPE_LIST_CHECKBOX) }
            selectLayoutApps.value = SelectLayoutApps.Loaded(apps)
        }
    }

    fun onPickerTypeChanged(type: Int) {
        userSettings.appPickerType = type
    }

    fun onSelectLayoutModeToggled(): Boolean {
        val newValue = !userSettings.appPickerSelectLayoutMode
        userSettings.appPickerSelectLayoutMode = newValue
        return newValue
    }
}
