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
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

class SwitchBarViewModelTest : ShouldSpec(
    {
        val mockRepo = mockk<UserSettingsRepository>(relaxed = true)

        lateinit var viewModel: SwitchBarViewModel

        beforeEach {
            clearMocks(mockRepo)
            every { mockRepo.flow } returns MutableStateFlow(UserSettings())
            every { mockRepo.sampleSwitchBar } returns false
            viewModel = SwitchBarViewModel(mockRepo)
        }

        should("initial state has enabled = false") {
            viewModel.state.value shouldBe SwitchBarUiState(enabled = false)
        }

        should("initial state reflects repository sampleSwitchBar = true") {
            every { mockRepo.sampleSwitchBar } returns true
            every { mockRepo.flow } returns MutableStateFlow(UserSettings(sampleSwitchBar = true))
            val vm = SwitchBarViewModel(mockRepo)
            vm.state.value shouldBe SwitchBarUiState(enabled = true)
        }

        should("onSwitchChanged true writes to repository") {
            viewModel.onSwitchChanged(true)
            verify { mockRepo.sampleSwitchBar = true }
        }

        should("onSwitchChanged false writes to repository") {
            viewModel.onSwitchChanged(false)
            verify { mockRepo.sampleSwitchBar = false }
        }
    },
)
