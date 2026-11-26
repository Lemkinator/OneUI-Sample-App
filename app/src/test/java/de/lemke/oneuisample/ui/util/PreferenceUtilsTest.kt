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
import android.content.Intent
import android.os.Looper
import androidx.preference.Preference
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.HiltTestRule
import de.lemke.oneuisample.R
import de.lemke.oneuisample.ui.SettingsActivity
import dev.oneuiproject.oneui.preference.HorizontalRadioPreference
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
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
class PreferenceUtilsTest {
    @get:Rule(order = 0)
    val hiltRule = HiltTestRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    private fun withFragment(block: (SettingsActivity.SettingsFragment) -> Unit) {
        ActivityScenario.launch<SettingsActivity>(Intent(context, SettingsActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                block(activity.supportFragmentManager.findFragmentById(R.id.settings) as SettingsActivity.SettingsFragment)
            }
        }
    }

    @Test
    fun requirePreference_keyInScreen_returnsPreferenceAsRequestedType() {
        withFragment { it.requirePreference<HorizontalRadioPreference>("darkMode").key shouldBe "darkMode" }
    }

    @Test
    fun requirePreference_keyNotInScreen_throwsNamingKey() {
        withFragment { fragment ->
            shouldThrow<IllegalStateException> { fragment.requirePreference<Preference>("doesNotExist") }
                .message shouldBe "Preference doesNotExist is missing from the preference screen"
        }
    }
}
