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
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.MainActivity
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
class TransitionUtilsKtTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        ApplicationProvider
            .getApplicationContext<Application>()
            .getSharedPreferences(UserSettings.PREFS_NAME, Context.MODE_PRIVATE)
            .bypassOobe()
    }

    private fun withMainActivity(block: (MainActivity) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Application>()
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity(block)
        }
    }

    @Test
    fun `overrideFadeOpenTransition api34+ branch does not throw`() {
        withMainActivity { it.overrideFadeOpenTransition() }
    }

    @Test
    @Config(application = HiltTestApplication::class, sdk = [26])
    fun `overrideFadeOpenTransition legacy branch does not throw`() {
        withMainActivity { it.overrideFadeOpenTransition() }
    }

    @Test
    fun `finishWithFade api34+ branch does not throw`() {
        withMainActivity { it.finishWithFade() }
    }

    @Test
    @Config(application = HiltTestApplication::class, sdk = [26])
    fun `finishWithFade legacy branch does not throw`() {
        withMainActivity { it.finishWithFade() }
    }
}
