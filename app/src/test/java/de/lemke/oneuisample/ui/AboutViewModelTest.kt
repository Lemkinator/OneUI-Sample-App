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
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

class AboutViewModelTest : ShouldSpec(
    {
        val mockSettings = mockk<UserSettings>(relaxed = true)

        lateinit var viewModel: AboutViewModel

        beforeEach {
            clearMocks(mockSettings)
            every { mockSettings.flow } returns MutableStateFlow(UserSettingsSnapshot())
            every { mockSettings.devModeEnabled } returns false
            viewModel = AboutViewModel(mockSettings)
        }

        should("initial state has devModeEnabled = false") {
            viewModel.state.value shouldBe AboutUiState(devModeEnabled = false)
        }

        should("initial state reflects settings devModeEnabled = true") {
            every { mockSettings.devModeEnabled } returns true
            every { mockSettings.flow } returns MutableStateFlow(UserSettingsSnapshot(devModeEnabled = true))
            val vm = AboutViewModel(mockSettings)
            vm.state.value shouldBe AboutUiState(devModeEnabled = true)
        }

        should("onToggleDevMode toggles devModeEnabled via settings update") {
            val transformSlot = slot<UserSettingsSnapshot.() -> UserSettingsSnapshot>()
            every { mockSettings.update(capture(transformSlot)) } answers { }
            viewModel.onToggleDevMode()
            verify(exactly = 1) { mockSettings.update(any()) }
            UserSettingsSnapshot(devModeEnabled = false).run(transformSlot.captured).devModeEnabled shouldBe true
            UserSettingsSnapshot(devModeEnabled = true).run(transformSlot.captured).devModeEnabled shouldBe false
        }
    },
)
