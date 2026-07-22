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
package de.lemke.oneuisample.domain

import android.content.Context
import android.content.res.Resources
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.fakeUserSettings
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class CompleteOnboardingUseCaseTest : ShouldSpec(
    {
        lateinit var mockContext: Context
        lateinit var mockResources: Resources
        lateinit var settings: UserSettings
        lateinit var useCase: CompleteOnboardingUseCase

        beforeEach {
            mockContext = mockk()
            mockResources = mockk()
            settings = fakeUserSettings()
            useCase = CompleteOnboardingUseCase(mockContext, settings, UnconfinedTestDispatcher())
            every { mockContext.resources } returns mockResources
            every { mockResources.getInteger(R.integer.tos_version) } returns 2
        }

        should("update acceptedTosVersion to tos_version resource value") {
            useCase(versionCode = 10, versionName = "1.0")
            settings.acceptedTosVersion shouldBe 2
        }

        should("update lastVersionCode to provided versionCode") {
            useCase(versionCode = 7, versionName = "2.0")
            settings.lastVersionCode shouldBe 7
        }

        should("update lastVersionName to provided versionName") {
            useCase(versionCode = 1, versionName = "3.1.4")
            settings.lastVersionName shouldBe "3.1.4"
        }
    },
)
