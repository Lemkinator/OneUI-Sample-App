/*
 * Copyright 2024-2026 Leonard Lemke
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
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class CompleteOnboardingUseCaseTest : ShouldSpec(
    {
        lateinit var mockContext: Context
        lateinit var mockResources: Resources
        lateinit var mockRepo: UserSettingsRepository
        lateinit var useCase: CompleteOnboardingUseCase

        beforeEach {
            mockContext = mockk()
            mockResources = mockk()
            mockRepo = mockk(relaxed = true)
            useCase = CompleteOnboardingUseCase(mockContext, mockRepo)
            every { mockContext.resources } returns mockResources
            every { mockResources.getInteger(R.integer.tos_version) } returns 2
        }

        should("update acceptedTosVersion to tos_version resource value") {
            val transformSlot = slot<UserSettings.() -> UserSettings>()
            every { mockRepo.update(capture(transformSlot)) } answers { }

            useCase(versionCode = 10, versionName = "1.0")

            val result = UserSettings(acceptedTosVersion = -1).run(transformSlot.captured)
            result.acceptedTosVersion shouldBe 2
        }

        should("update lastVersionCode to provided versionCode") {
            val transformSlot = slot<UserSettings.() -> UserSettings>()
            every { mockRepo.update(capture(transformSlot)) } answers { }

            useCase(versionCode = 7, versionName = "2.0")

            val result = UserSettings().run(transformSlot.captured)
            result.lastVersionCode shouldBe 7
        }

        should("update lastVersionName to provided versionName") {
            val transformSlot = slot<UserSettings.() -> UserSettings>()
            every { mockRepo.update(capture(transformSlot)) } answers { }

            useCase(versionCode = 1, versionName = "3.1.4")

            val result = UserSettings().run(transformSlot.captured)
            result.lastVersionName shouldBe "3.1.4"
        }
    },
)
