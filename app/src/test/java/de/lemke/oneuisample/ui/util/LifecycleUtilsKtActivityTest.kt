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
package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
class LifecycleUtilsKtActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withActivity(block: (MainActivity) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity(block)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `launchAndRepeatWithLifecycle executes block when activity is started`() {
        var executed = false
        withActivity { activity ->
            activity.launchAndRepeatWithLifecycle { executed = true }
        }
        executed shouldBe true
    }

    @Test
    fun `collectState delivers initial flow value to callback`() {
        val flow = MutableStateFlow(42)
        var received = -1
        withActivity { activity ->
            activity.collectState(flow) { received = it }
        }
        received shouldBe 42
    }

    @Test
    fun `collectEvents delivers flow emission to callback`() {
        val flow = MutableStateFlow("hello")
        var received = ""
        withActivity { activity ->
            activity.collectEvents(flow) { received = it }
        }
        received shouldBe "hello"
    }
}
