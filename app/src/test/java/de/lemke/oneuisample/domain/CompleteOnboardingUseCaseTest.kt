package de.lemke.oneuisample.domain

import android.content.Context
import android.content.res.Resources
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class CompleteOnboardingUseCaseTest : ShouldSpec(
    {
        val mockContext = mockk<Context>()
        val mockResources = mockk<Resources>()
        val mockRepo = mockk<UserSettingsRepository>(relaxed = true)
        val useCase = CompleteOnboardingUseCase(mockContext, mockRepo)

        beforeEach {
            clearMocks(mockRepo)
            every { mockContext.resources } returns mockResources
            every { mockResources.getInteger(R.integer.tos_version) } returns 2
        }

        should("update acceptedTosVersion to tos_version resource value") {
            val transformSlot = slot<UserSettings.() -> UserSettings>()
            every { mockRepo.update(capture(transformSlot)) } answers { Unit }

            useCase(versionCode = 10, versionName = "1.0")

            val result = UserSettings(acceptedTosVersion = -1).run(transformSlot.captured)
            result.acceptedTosVersion shouldBe 2
        }

        should("update lastVersionCode to provided versionCode") {
            val transformSlot = slot<UserSettings.() -> UserSettings>()
            every { mockRepo.update(capture(transformSlot)) } answers { Unit }

            useCase(versionCode = 7, versionName = "2.0")

            val result = UserSettings().run(transformSlot.captured)
            result.lastVersionCode shouldBe 7
        }

        should("update lastVersionName to provided versionName") {
            val transformSlot = slot<UserSettings.() -> UserSettings>()
            every { mockRepo.update(capture(transformSlot)) } answers { Unit }

            useCase(versionCode = 1, versionName = "3.1.4")

            val result = UserSettings().run(transformSlot.captured)
            result.lastVersionName shouldBe "3.1.4"
        }
    },
)
