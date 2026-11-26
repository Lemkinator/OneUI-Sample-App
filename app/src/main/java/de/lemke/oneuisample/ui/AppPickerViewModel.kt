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
import de.lemke.oneuisample.data.UserSettingsSnapshot
import de.lemke.oneuisample.di.IoDispatcher
import de.lemke.oneuisample.ui.util.AppListSource
import de.lemke.oneuisample.ui.util.ListTypes
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

sealed interface AppListState {
    data object Loading : AppListState

    class Loaded(
        val apps: List<AppInfoData>,
    ) : AppListState
}

data class AppPickerUiState(
    val listType: ListTypes = ListTypes.entries.first(),
    val isSelectLayoutMode: Boolean = false,
    val apps: AppListState = AppListState.Loading,
)

private sealed interface AppListKind {
    val listType: ListTypes

    data object SelectLayout : AppListKind {
        override val listType = ListTypes.TYPE_LIST_CHECKBOX
    }

    data class Simple(
        override val listType: ListTypes,
    ) : AppListKind
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppPickerViewModel @Inject constructor(
    private val userSettings: UserSettings,
    private val appListSource: AppListSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val state: StateFlow<AppPickerUiState> =
        userSettings.flow
            .map { it.appListKind() }
            .distinctUntilChanged()
            .flatMapLatest { kind ->
                loadApps(kind).combine(listTypeOf(kind)) { apps, listType ->
                    AppPickerUiState(
                        listType = listType,
                        isSelectLayoutMode = kind is AppListKind.SelectLayout,
                        apps = apps,
                    )
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                AppPickerUiState(
                    listType = listTypeAt(userSettings.appPickerType),
                    isSelectLayoutMode = userSettings.appPickerSelectLayoutMode,
                ),
            )

    fun onPickerTypeChanged(type: Int) {
        userSettings.appPickerType = type
    }

    fun onSelectLayoutModeToggled(): Boolean {
        val newValue = !userSettings.appPickerSelectLayoutMode
        userSettings.appPickerSelectLayoutMode = newValue
        return newValue
    }

    private fun listTypeOf(kind: AppListKind): Flow<ListTypes> =
        when (kind) {
            is AppListKind.Simple -> flowOf(kind.listType)
            AppListKind.SelectLayout -> userSettings.flow.map { listTypeAt(it.appPickerType) }.distinctUntilChanged()
        }

    private fun loadApps(kind: AppListKind): Flow<AppListState> =
        flow {
            emit(AppListState.Loading)
            emit(AppListState.Loaded(withContext(ioDispatcher) { appListSource.load(kind.listType) }))
        }
}

private fun listTypeAt(pickerType: Int): ListTypes = ListTypes.entries.getOrElse(pickerType) { ListTypes.entries.first() }

private fun UserSettingsSnapshot.appListKind(): AppListKind =
    if (appPickerSelectLayoutMode) AppListKind.SelectLayout else AppListKind.Simple(listTypeAt(appPickerType))
