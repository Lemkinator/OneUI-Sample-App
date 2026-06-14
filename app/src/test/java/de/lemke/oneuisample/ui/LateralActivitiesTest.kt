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

package de.lemke.oneuisample.ui

import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LateralActivitiesTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

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
}
