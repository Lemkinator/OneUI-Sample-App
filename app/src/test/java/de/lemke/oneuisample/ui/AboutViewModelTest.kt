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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest : ShouldSpec(
    {
        lateinit var settings: UserSettings
        lateinit var viewModel: AboutViewModel

        beforeEach {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            settings = fakeUserSettings()
            viewModel = AboutViewModel(settings)
        }

        afterEach {
            Dispatchers.resetMain()
        }

        should("initial state has devModeEnabled = false") {
            viewModel.state.value shouldBe AboutUiState(devModeEnabled = false)
        }

        should("initial state reflects settings devModeEnabled = true") {
            settings.devModeEnabled = true
            val vm = AboutViewModel(settings)
            vm.state.value shouldBe AboutUiState(devModeEnabled = true)
        }

        should("onToggleDevMode toggles devModeEnabled via settings update") {
            settings.devModeEnabled shouldBe false
            viewModel.state.test {
                awaitItem() shouldBe AboutUiState(devModeEnabled = false)
                viewModel.onToggleDevMode()
                awaitItem() shouldBe AboutUiState(devModeEnabled = true)
                viewModel.onToggleDevMode()
                awaitItem() shouldBe AboutUiState(devModeEnabled = false)
            }
        }
    },
)
