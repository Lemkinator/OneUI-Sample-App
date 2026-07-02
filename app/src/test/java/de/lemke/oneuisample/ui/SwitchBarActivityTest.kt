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

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SwitchBarActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    private fun launch(block: SwitchBarActivity.() -> Unit = {}) {
        ActivityScenario.launch<SwitchBarActivity>(Intent(context, SwitchBarActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun onSwitchChanged_true_updatesViewModel() {
        prefs.edit().putBoolean("sampleSwitchBar", false).commit()
        launch {
            onSwitchChanged(mockk(relaxed = true), true)
        }
        // onSwitchChanged → viewModel.onSwitchChanged(true) → userSettings.sampleSwitchBar = true
        prefs.getBoolean("sampleSwitchBar", false) shouldBe true
    }

    @Test
    fun onSwitchChanged_false_updatesViewModel() {
        // Pre-set to true so toggling to false produces a meaningful observable change
        prefs.edit().putBoolean("sampleSwitchBar", true).commit()
        launch {
            onSwitchChanged(mockk(relaxed = true), false)
        }
        prefs.getBoolean("sampleSwitchBar", true) shouldBe false
    }
}
