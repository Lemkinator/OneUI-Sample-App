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

import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.fakeUserSettings
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class AppPickerViewModelTest : ShouldSpec(
    {
        lateinit var settings: UserSettings
        lateinit var viewModel: AppPickerViewModel

        beforeEach {
            settings = fakeUserSettings()
            viewModel = AppPickerViewModel(settings)
        }

        should("initial state has pickerType = 0") {
            viewModel.state.value shouldBe AppPickerUiState(pickerType = 0)
        }

        should("initial state reflects settings appPickerType = 1") {
            settings.appPickerType = 1
            val vm = AppPickerViewModel(settings)
            vm.state.value shouldBe AppPickerUiState(pickerType = 1)
        }

        should("onPickerTypeChanged writes to settings") {
            viewModel.onPickerTypeChanged(2)
            settings.appPickerType shouldBe 2
        }

        should("initial state reflects settings appPickerSelectLayoutMode = true") {
            settings.appPickerSelectLayoutMode = true
            val vm = AppPickerViewModel(settings)
            vm.state.value shouldBe AppPickerUiState(isSelectLayoutMode = true)
        }

        should("onSelectLayoutModeToggled flips settings value and returns the new value") {
            settings.appPickerSelectLayoutMode = false
            viewModel.onSelectLayoutModeToggled() shouldBe true
            settings.appPickerSelectLayoutMode shouldBe true
        }
    },
)
