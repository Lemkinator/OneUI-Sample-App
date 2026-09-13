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

import android.app.Activity
import android.content.Intent
import android.os.Looper
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.descendants
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.airbnb.lottie.LottieAnimationView
import com.github.takahirom.roborazzi.captureRoboImage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import dev.oneuiproject.oneui.design.R as designR

// sdk = [36]: Robolectric 4.16.1 max supported SDK; bump when 4.17+ adds SDK 37.
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LateralActivitiesScreenshotTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<HiltTestApplication>()

    private inline fun <reified T : Activity> captureScreenshot(
        fileName: String,
        crossinline pin: (T) -> Unit = {},
    ) {
        ActivityScenario.launch<T>(Intent(context, T::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            // Lottie drives its own Choreographer-based animator, independent of the Looper
            // queue idle() drains above — an autoplaying/looping view is otherwise captured at
            // a timing-dependent frame. Pin every one to frame 0 for a deterministic screenshot.
            scenario.onActivity { activity ->
                (activity.window.decorView as ViewGroup)
                    .descendants
                    .filterIsInstance<LottieAnimationView>()
                    .forEach {
                        it.pauseAnimation()
                        it.progress = 0f
                    }
                pin(activity)
            }
            onView(isRoot()).captureRoboImage(fileName)
        }
    }

    // BuildConfig.VERSION_NAME tracks the app's release version, so any golden that shows it
    // would break on every version bump - pin it to a fixed value instead of re-recording.
    private val pinnedVersion = "1.0.0"

    // Settings

    @Test
    fun settings_light() = captureScreenshot<SettingsActivity>("src/test/screenshots/settings_light.png")

    @Test
    @Config(qualifiers = "+night")
    fun settings_dark() = captureScreenshot<SettingsActivity>("src/test/screenshots/settings_dark.png")

    // About

    private fun AboutActivity.pinVersion() {
        findViewById<TextView>(designR.id.app_info_version).text =
            getString(designR.string.oui_des_version_info, pinnedVersion)
    }

    @Test
    fun about_light() = captureScreenshot<AboutActivity>("src/test/screenshots/about_light.png") { it.pinVersion() }

    @Test
    @Config(qualifiers = "+night")
    fun about_dark() = captureScreenshot<AboutActivity>("src/test/screenshots/about_dark.png") { it.pinVersion() }

    // Custom About

    private fun CustomAboutActivity.pinVersion() {
        val text = getString(R.string.version, pinnedVersion)
        findViewById<TextView>(R.id.aboutHeaderAppVersion).text = text
        findViewById<TextView>(R.id.aboutBottomAppVersion).text = text
    }

    @Test
    fun customAbout_light() = captureScreenshot<CustomAboutActivity>("src/test/screenshots/custom_about_light.png") { it.pinVersion() }

    @Test
    @Config(qualifiers = "+night")
    fun customAbout_dark() = captureScreenshot<CustomAboutActivity>("src/test/screenshots/custom_about_dark.png") { it.pinVersion() }

    // OOBE

    @Test
    fun oobe_light() = captureScreenshot<OOBEActivity>("src/test/screenshots/oobe_light.png")

    @Test
    @Config(qualifiers = "+night")
    fun oobe_dark() = captureScreenshot<OOBEActivity>("src/test/screenshots/oobe_dark.png")

    // SwitchBar

    @Test
    fun switchBar_light() = captureScreenshot<SwitchBarActivity>("src/test/screenshots/switch_bar_light.png")

    @Test
    @Config(qualifiers = "+night")
    fun switchBar_dark() = captureScreenshot<SwitchBarActivity>("src/test/screenshots/switch_bar_dark.png")

    // SeekBar

    @Test
    fun seekBar_light() = captureScreenshot<SeekBarActivity>("src/test/screenshots/seek_bar_light.png")

    @Test
    @Config(qualifiers = "+night")
    fun seekBar_dark() = captureScreenshot<SeekBarActivity>("src/test/screenshots/seek_bar_dark.png")

    // App Picker

    @Test
    fun appPicker_light() = captureScreenshot<AppPickerActivity>("src/test/screenshots/app_picker_light.png")

    @Test
    @Config(qualifiers = "+night")
    fun appPicker_dark() = captureScreenshot<AppPickerActivity>("src/test/screenshots/app_picker_dark.png")
}
