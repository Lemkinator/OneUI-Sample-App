package de.lemke.oneuisample.ui

import androidx.lifecycle.SavedStateHandle
import de.lemke.oneuisample.domain.CompleteOnboardingUseCase
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk

class OOBEViewModelTest : ShouldSpec(
    {
        val completeOnboarding = mockk<CompleteOnboardingUseCase>()

        lateinit var viewModel: OOBEViewModel

        beforeEach {
            clearMocks(completeOnboarding)
            coJustRun { completeOnboarding(any(), any()) }
            viewModel = OOBEViewModel(SavedStateHandle(), completeOnboarding)
        }

        should("isAccepting starts as false") {
            viewModel.isAccepting.value shouldBe false
        }

        should("onAcceptTos sets isAccepting to true") {
            viewModel.onAcceptTos()
            viewModel.isAccepting.value shouldBe true
        }

        should("onAcceptTos calls completeOnboarding") {
            viewModel.onAcceptTos()
            coVerify(atLeast = 1) { completeOnboarding(any(), any()) }
        }

        should("subsequent onAcceptTos while accepting is ignored") {
            viewModel.onAcceptTos()
            viewModel.onAcceptTos()
            coVerify(exactly = 1) { completeOnboarding(any(), any()) }
        }
    },
)
