package de.lemke.oneuisample.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.lemke.oneuisample.TestDispatcherListener
import de.lemke.oneuisample.domain.CompleteOnboardingUseCase
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_CODE
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_NAME
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

        @OptIn(ExperimentalCoroutinesApi::class)
        should("onAcceptTos emits NavigateToMain event after delay") {
            viewModel.events.test {
                viewModel.onAcceptTos()
                TestDispatcherListener.scheduler.advanceUntilIdle()
                awaitItem() shouldBe OOBEEvent.NavigateToMain
            }
        }

        should("uses versionCode and versionName from SavedStateHandle when present") {
            val handle = SavedStateHandle(mapOf(EXTRA_VERSION_CODE to 5, EXTRA_VERSION_NAME to "2.0"))
            val vm = OOBEViewModel(handle, completeOnboarding)
            vm.isAccepting.value shouldBe false
        }
    },
)
