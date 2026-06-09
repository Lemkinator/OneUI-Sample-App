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
            every { mockRepo.darkMode } returns false
            every { mockRepo.autoDarkMode } returns true
            every { mockRepo.devModeEnabled } returns false
            every { mockRepo.sampleSwitchBar } returns false
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
