/*
 * Copyright 2024-2026 Leonard Lemke
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
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

class AppPickerViewModelTest : ShouldSpec(
    {
        val mockRepo = mockk<UserSettingsRepository>(relaxed = true)

        lateinit var viewModel: AppPickerViewModel

        beforeEach {
            clearMocks(mockRepo)
            every { mockRepo.flow } returns MutableStateFlow(UserSettings())
            every { mockRepo.appPickerType } returns 0
            viewModel = AppPickerViewModel(mockRepo)
        }

        should("initial state has pickerType = 0") {
            viewModel.state.value shouldBe AppPickerUiState(pickerType = 0)
        }

        should("initial state reflects repository appPickerType = 1") {
            every { mockRepo.appPickerType } returns 1
            every { mockRepo.flow } returns MutableStateFlow(UserSettings(appPickerType = 1))
            val vm = AppPickerViewModel(mockRepo)
            vm.state.value shouldBe AppPickerUiState(pickerType = 1)
        }

        should("onPickerTypeChanged writes to repository") {
            viewModel.onPickerTypeChanged(2)
            verify { mockRepo.appPickerType = 2 }
        }
    },
)
