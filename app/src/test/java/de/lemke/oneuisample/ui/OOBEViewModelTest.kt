package de.lemke.oneuisample.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.lemke.oneuisample.domain.CompleteOnboardingUseCase
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_CODE
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_NAME
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class OOBEViewModelTest : ShouldSpec(
    {
        val testDispatcher = UnconfinedTestDispatcher()
        val completeOnboarding = mockk<CompleteOnboardingUseCase>()

        lateinit var viewModel: OOBEViewModel

        beforeEach {
            Dispatchers.setMain(testDispatcher)
            clearMocks(completeOnboarding)
            coJustRun { completeOnboarding(any(), any()) }
            viewModel = OOBEViewModel(SavedStateHandle(), completeOnboarding)
        }

        afterEach {
            Dispatchers.resetMain()
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

        should("onAcceptTos emits NavigateToMain event after delay") {
            viewModel.events.test {
                viewModel.onAcceptTos()
                testDispatcher.scheduler.advanceUntilIdle()
                awaitItem() shouldBe OOBEEvent.NavigateToMain
            }
        }

        should("construction with version extras in SavedStateHandle does not throw and starts not accepting") {
            val handle = SavedStateHandle(mapOf(EXTRA_VERSION_CODE to 5, EXTRA_VERSION_NAME to "2.0"))
            val vm = OOBEViewModel(handle, completeOnboarding)
            vm.isAccepting.value shouldBe false
        }
    },
)
