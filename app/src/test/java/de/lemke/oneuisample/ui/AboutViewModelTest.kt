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
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

class AboutViewModelTest : ShouldSpec(
    {
        val mockRepo = mockk<UserSettingsRepository>(relaxed = true)

        lateinit var viewModel: AboutViewModel

        beforeEach {
            clearMocks(mockRepo)
            every { mockRepo.flow } returns MutableStateFlow(UserSettings())
            every { mockRepo.devModeEnabled } returns false
            viewModel = AboutViewModel(mockRepo)
        }

        should("initial state has devModeEnabled = false") {
            viewModel.state.value shouldBe AboutUiState(devModeEnabled = false)
        }

        should("initial state reflects repository devModeEnabled = true") {
            every { mockRepo.devModeEnabled } returns true
            every { mockRepo.flow } returns MutableStateFlow(UserSettings(devModeEnabled = true))
            val vm = AboutViewModel(mockRepo)
            vm.state.value shouldBe AboutUiState(devModeEnabled = true)
        }

        should("onToggleDevMode toggles devModeEnabled via repository update") {
            val transformSlot = slot<UserSettings.() -> UserSettings>()
            every { mockRepo.update(capture(transformSlot)) } answers { }
            viewModel.onToggleDevMode()
            verify(exactly = 1) { mockRepo.update(any()) }
            UserSettings(devModeEnabled = false).run(transformSlot.captured).devModeEnabled shouldBe true
            UserSettings(devModeEnabled = true).run(transformSlot.captured).devModeEnabled shouldBe false
        }
    },
)
