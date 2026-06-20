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

class SettingsViewModelTest : ShouldSpec(
    {
        val mockRepo = mockk<UserSettingsRepository>(relaxed = true)
        val settingsFlow = MutableStateFlow(UserSettings())

        lateinit var viewModel: SettingsViewModel

        beforeEach {
            clearMocks(mockRepo)
            every { mockRepo.flow } returns settingsFlow
            // autoDarkMode defaults to true in UserSettings; relaxed mock returns false, so must stub explicitly.
            // Other boolean properties (darkMode, devModeEnabled, sampleSwitchBar) default to false,
            // and relaxed mock already returns false — no stubs needed.
            every { mockRepo.autoDarkMode } returns true
            viewModel = SettingsViewModel(mockRepo)
        }

        should("initial state matches repository defaults") {
            viewModel.state.value shouldBe
                SettingsUiState(
                    darkMode = false,
                    autoDarkMode = true,
                    devModeEnabled = false,
                    sampleSwitchBar = false,
                )
        }

        should("onDarkModeChanged writes to repository") {
            viewModel.onDarkModeChanged(true)
            verify { mockRepo.darkMode = true }
        }

        should("onAutoDarkModeChanged writes to repository") {
            viewModel.onAutoDarkModeChanged(false)
            verify { mockRepo.autoDarkMode = false }
        }

        should("onSampleSwitchBarChanged writes to repository") {
            viewModel.onSampleSwitchBarChanged(true)
            verify { mockRepo.sampleSwitchBar = true }
        }
    },
)
