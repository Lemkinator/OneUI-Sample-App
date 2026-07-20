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
import de.lemke.oneuisample.data.UserSettingsSnapshot
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

class AppPickerViewModelTest : ShouldSpec(
    {
        val mockSettings = mockk<UserSettings>(relaxed = true)

        lateinit var viewModel: AppPickerViewModel

        beforeEach {
            clearMocks(mockSettings)
            every { mockSettings.flow } returns MutableStateFlow(UserSettingsSnapshot())
            every { mockSettings.appPickerType } returns 0
            every { mockSettings.appPickerSelectLayoutMode } returns false
            viewModel = AppPickerViewModel(mockSettings)
        }

        should("initial state has pickerType = 0") {
            viewModel.state.value shouldBe AppPickerUiState(pickerType = 0)
        }

        should("initial state reflects settings appPickerType = 1") {
            every { mockSettings.appPickerType } returns 1
            every { mockSettings.flow } returns MutableStateFlow(UserSettingsSnapshot(appPickerType = 1))
            val vm = AppPickerViewModel(mockSettings)
            vm.state.value shouldBe AppPickerUiState(pickerType = 1)
        }

        should("onPickerTypeChanged writes to settings") {
            viewModel.onPickerTypeChanged(2)
            verify { mockSettings.appPickerType = 2 }
        }

        should("initial state reflects settings appPickerSelectLayoutMode = true") {
            every { mockSettings.appPickerSelectLayoutMode } returns true
            every { mockSettings.flow } returns MutableStateFlow(UserSettingsSnapshot(appPickerSelectLayoutMode = true))
            val vm = AppPickerViewModel(mockSettings)
            vm.state.value shouldBe AppPickerUiState(isSelectLayoutMode = true)
        }

        should("onSelectLayoutModeToggled flips settings value and returns the new value") {
            every { mockSettings.appPickerSelectLayoutMode } returns false
            viewModel.onSelectLayoutModeToggled() shouldBe true
            verify { mockSettings.appPickerSelectLayoutMode = true }
        }
    },
)
