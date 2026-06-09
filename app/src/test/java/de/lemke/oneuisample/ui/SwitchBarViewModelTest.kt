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
