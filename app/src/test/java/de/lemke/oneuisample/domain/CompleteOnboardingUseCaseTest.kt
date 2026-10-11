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

import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.fakeUserSettings
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class CompleteOnboardingUseCaseTest : ShouldSpec(
    {
        lateinit var settings: UserSettings
        lateinit var useCase: CompleteOnboardingUseCase

        beforeEach {
            settings = fakeUserSettings()
            useCase =
                CompleteOnboardingUseCase(
                    tosVersion = 2,
                    settings,
                    CheckAppStartUseCase(settings, tosVersion = 2),
                    UnconfinedTestDispatcher(),
                )
        }

        should("update acceptedTosVersion to the injected TOS version") {
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
