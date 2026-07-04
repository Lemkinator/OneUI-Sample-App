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

import android.content.Intent
import android.os.Looper
import android.view.MenuItem
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import io.mockk.every
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
class LateralActivitiesTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<HiltTestApplication>()

    private inline fun <reified T : android.app.Activity> launch() {
        ActivityScenario.launch<T>(Intent(context, T::class.java)).use {
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun aboutActivity_launchesWithoutCrash() = launch<AboutActivity>()

    @Test
    fun oobeActivity_launchesWithoutCrash() = launch<OOBEActivity>()

    @Test
    fun settingsActivity_launchesWithoutCrash() = launch<SettingsActivity>()

    @Test
    fun customAboutActivity_launchesWithoutCrash() = launch<CustomAboutActivity>()

    @Test
    fun switchBarActivity_launchesWithoutCrash() = launch<SwitchBarActivity>()

    @Test
    fun seekBarActivity_launchesWithoutCrash() = launch<SeekBarActivity>()

    @Test
    fun libsActivity_launchesWithoutCrash() = launch<LibsActivity>()

    @Test
    fun appPickerActivity_launchesWithoutCrash() = launch<AppPickerActivity>()

    @Test
    fun aboutActivity_changeStatus_cyclesAllStatuses() {
        ActivityScenario.launch<AboutActivity>(Intent(context, AboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            repeat(8) {
                scenario.onActivity { it.changeStatus() }
                shadowOf(Looper.getMainLooper()).idle()
            }
        }
    }

    @Test
    fun appPickerActivity_onOptionsItemSelected_search_returnsTrue() {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val item = mockk<MenuItem> { every { itemId } returns R.id.menu_app_picker_search }
                activity.onOptionsItemSelected(item) shouldBe true
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun appPickerActivity_onOptionsItemSelected_unknown_returnsFalse() {
        ActivityScenario.launch<AppPickerActivity>(Intent(context, AppPickerActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val item = mockk<MenuItem> { every { itemId } returns -1 }
                activity.onOptionsItemSelected(item) shouldBe false
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
