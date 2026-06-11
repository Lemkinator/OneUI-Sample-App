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
            // Other boolean properties (darkMode, devModeEnabled, sampleSwitchBar) default to false
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
