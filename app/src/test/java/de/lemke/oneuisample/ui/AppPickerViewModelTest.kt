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

import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoDataImpl
import app.cash.turbine.test
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.fakeUserSettings
import de.lemke.oneuisample.ui.util.AppListSource
import de.lemke.oneuisample.ui.util.ListTypes
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher

private val FAKE_APPS = listOf(AppInfoDataImpl(AppInfo("de.lemke.first", "")), AppInfoDataImpl(AppInfo("de.lemke.second", "")))

@OptIn(ExperimentalCoroutinesApi::class)
class AppPickerViewModelTest : ShouldSpec(
    {
        lateinit var settings: UserSettings
        lateinit var viewModel: AppPickerViewModel
        lateinit var scheduler: TestCoroutineScheduler
        val requestedTypes = mutableListOf<ListTypes>()

        fun newViewModel() =
            AppPickerViewModel(
                settings,
                AppListSource { listType ->
                    requestedTypes += listType
                    FAKE_APPS
                },
                StandardTestDispatcher(scheduler),
            )

        beforeEach {
            scheduler = UnconfinedTestDispatcher().scheduler
            requestedTypes.clear()
            settings = fakeUserSettings()
            viewModel = newViewModel()
        }

        should("initial state has pickerType = 0") {
            viewModel.state.value shouldBe AppPickerUiState(pickerType = 0)
        }

        should("initial state reflects settings appPickerType = 1") {
            settings.appPickerType = 1
            val vm = newViewModel()
            vm.state.value shouldBe AppPickerUiState(pickerType = 1)
        }

        should("onPickerTypeChanged writes to settings") {
            viewModel.state.test {
                awaitItem() shouldBe AppPickerUiState(pickerType = 0)
                viewModel.onPickerTypeChanged(2)
                settings.appPickerType shouldBe 2
                awaitItem() shouldBe AppPickerUiState(pickerType = 2)
            }
        }

        should("initial state reflects settings appPickerSelectLayoutMode = true") {
            settings.appPickerSelectLayoutMode = true
            val vm = newViewModel()
            vm.state.value shouldBe AppPickerUiState(isSelectLayoutMode = true)
        }

        should("onSelectLayoutModeToggled flips settings value and returns the new value") {
            settings.appPickerSelectLayoutMode = false
            viewModel.state.test {
                awaitItem() shouldBe AppPickerUiState(isSelectLayoutMode = false)
                viewModel.onSelectLayoutModeToggled() shouldBe true
                settings.appPickerSelectLayoutMode shouldBe true
                awaitItem() shouldBe AppPickerUiState(isSelectLayoutMode = true)
            }
        }

        should("selectLayoutApps stays Loading and loads nothing until onSelectLayoutShown") {
            viewModel.state.test {
                awaitItem().selectLayoutApps shouldBe SelectLayoutApps.Loading
                scheduler.runCurrent()
                requestedTypes shouldBe emptyList()
            }
        }

        should("onSelectLayoutShown loads the checkbox list once and exposes it as Loaded") {
            viewModel.state.test {
                awaitItem().selectLayoutApps shouldBe SelectLayoutApps.Loading
                viewModel.onSelectLayoutShown()
                viewModel.onSelectLayoutShown()
                scheduler.runCurrent()
                awaitItem().selectLayoutApps shouldBe SelectLayoutApps.Loaded(FAKE_APPS)
            }
            requestedTypes shouldBe listOf(ListTypes.TYPE_LIST_CHECKBOX)
        }
    },
)
