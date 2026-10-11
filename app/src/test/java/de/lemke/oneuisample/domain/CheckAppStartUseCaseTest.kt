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

import android.app.Application
import android.util.Log
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.fakeUserSettings
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [36])
class CheckAppStartUseCaseTest {
    private lateinit var settings: UserSettings
    private lateinit var checkAppStart: CheckAppStartUseCase

    @Before
    fun setUp() {
        settings = fakeUserSettings()
        checkAppStart = CheckAppStartUseCase(settings, tosVersion = 3)
    }

    @Test
    fun `returns FIRST_TIME when no version was recorded`() {
        checkAppStart(versionCode = 10, versionName = "1.0").result shouldBe AppStartResult.FIRST_TIME
    }

    @Test
    fun `returns FIRST_TIME_VERSION on upgrade`() {
        settings.lastVersionCode = 5
        checkAppStart(versionCode = 10, versionName = "1.0").result shouldBe AppStartResult.FIRST_TIME_VERSION
    }

    @Test
    fun `returns NORMAL on same version`() {
        settings.lastVersionCode = 10
        checkAppStart(versionCode = 10, versionName = "1.0").result shouldBe AppStartResult.NORMAL
    }

    @Test
    fun `returns NORMAL and warns on downgrade`() {
        settings.lastVersionCode = 12
        checkAppStart(versionCode = 10, versionName = "1.0").result shouldBe AppStartResult.NORMAL
        ShadowLog.getLogsForTag("AppStart").filter { it.type == Log.WARN }.map { it.msg } shouldContainExactly
            listOf(
                "Current version code (10) is less than the one recognized on last startup (12). ",
                "Defensively assuming normal app start.",
            )
    }

    @Test
    fun `carries the injected TOS version and the recorded state`() {
        settings.lastVersionCode = 4
        settings.lastVersionName = "0.4"
        settings.acceptedTosVersion = 2
        val appStart = checkAppStart(versionCode = 10, versionName = "1.0")
        appStart.tosVersion shouldBe 3
        appStart.acceptedTosVersion shouldBe 2
        appStart.lastVersionCode shouldBe 4
        appStart.lastVersionName shouldBe "0.4"
        appStart.versionCode shouldBe 10
        appStart.versionName shouldBe "1.0"
        appStart.tosAccepted shouldBe false
    }

    @Test
    fun `recordLaunchedVersion records the version for the next check`() {
        settings.lastVersionCode = 4
        settings.lastVersionName = "0.4"
        checkAppStart.recordLaunchedVersion(versionCode = 10, versionName = "1.0")
        settings.lastVersionCode shouldBe 10
        settings.lastVersionName shouldBe "1.0"
        checkAppStart(versionCode = 10, versionName = "1.0").result shouldBe AppStartResult.NORMAL
    }
}
