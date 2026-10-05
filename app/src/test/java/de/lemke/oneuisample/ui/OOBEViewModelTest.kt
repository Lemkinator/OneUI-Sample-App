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

import androidx.lifecycle.SavedStateHandle
import de.lemke.oneuisample.BuildConfig
import de.lemke.oneuisample.domain.CompleteOnboardingUseCase
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_CODE
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_NAME
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class OOBEViewModelTest : ShouldSpec(
    {
        lateinit var mainScheduler: TestCoroutineScheduler
        val completeOnboarding = mockk<CompleteOnboardingUseCase>()

        lateinit var viewModel: OOBEViewModel

        fun TestCoroutineScheduler.advanceBy(millis: Int) {
            advanceTimeBy(millis.milliseconds)
            runCurrent()
        }

        beforeEach {
            mainScheduler = UnconfinedTestDispatcher().scheduler
            clearMocks(completeOnboarding)
            coJustRun { completeOnboarding(any(), any()) }
            viewModel = OOBEViewModel(SavedStateHandle(mapOf(EXTRA_VERSION_CODE to 5, EXTRA_VERSION_NAME to "2.0")), completeOnboarding)
        }

        should("tosAcceptance starts Idle") {
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Idle
        }

        should("onAcceptTos completes onboarding with the version from the extras") {
            viewModel.onAcceptTos()
            coVerify(exactly = 1) { completeOnboarding(5, "2.0") }
        }

        should("onAcceptTos completes onboarding with the build version without extras") {
            OOBEViewModel(SavedStateHandle(), completeOnboarding).onAcceptTos()
            coVerify(exactly = 1) { completeOnboarding(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME) }
        }

        should("onAcceptTos stays Accepting until 500 ms have passed, then turns Accepted") {
            viewModel.onAcceptTos()
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Accepting
            mainScheduler.advanceBy(499)
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Accepting
            mainScheduler.advanceBy(1)
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Accepted
        }

        should("onAcceptTos while Accepting is ignored") {
            viewModel.onAcceptTos()
            viewModel.onAcceptTos()
            coVerify(exactly = 1) { completeOnboarding(5, "2.0") }
        }

        should("onAcceptTos after Accepted is ignored") {
            viewModel.onAcceptTos()
            mainScheduler.advanceBy(500)
            viewModel.onAcceptTos()
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Accepted
            coVerify(exactly = 1) { completeOnboarding(5, "2.0") }
        }

        should("onTosAcceptedHandled moves Accepted to Navigated") {
            viewModel.onAcceptTos()
            mainScheduler.advanceBy(500)
            viewModel.onTosAcceptedHandled()
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Navigated
        }

        should("onTosAcceptedHandled keeps Idle and Accepting") {
            viewModel.onTosAcceptedHandled()
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Idle
            viewModel.onAcceptTos()
            viewModel.onTosAcceptedHandled()
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Accepting
        }

        should("onAcceptTos after Navigated is ignored") {
            viewModel.onAcceptTos()
            mainScheduler.advanceBy(500)
            viewModel.onTosAcceptedHandled()
            viewModel.onAcceptTos()
            viewModel.tosAcceptance.value shouldBe TosAcceptance.Navigated
            coVerify(exactly = 1) { completeOnboarding(5, "2.0") }
        }
    },
)
