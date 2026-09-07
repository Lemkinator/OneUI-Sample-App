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

import app.cash.turbine.test
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.fakeUserSettings
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SettingsViewModelTest : ShouldSpec(
    {
        lateinit var settings: UserSettings
        lateinit var viewModel: SettingsViewModel

        beforeEach {
            settings = fakeUserSettings()
            viewModel = SettingsViewModel(settings)
        }

        should("initial state matches settings defaults") {
            viewModel.state.value shouldBe SettingsUiState(devModeEnabled = false, sampleSwitchBar = false)
        }

        should("state reflects devModeEnabled written from elsewhere") {
            viewModel.state.test {
                awaitItem() shouldBe SettingsUiState(devModeEnabled = false, sampleSwitchBar = false)
                settings.devModeEnabled = true
                awaitItem() shouldBe SettingsUiState(devModeEnabled = true, sampleSwitchBar = false)
            }
        }

        should("state reflects sampleSwitchBar written from elsewhere") {
            viewModel.state.test {
                awaitItem() shouldBe SettingsUiState(devModeEnabled = false, sampleSwitchBar = false)
                settings.sampleSwitchBar = true
                awaitItem() shouldBe SettingsUiState(devModeEnabled = false, sampleSwitchBar = true)
            }
        }
    },
)
