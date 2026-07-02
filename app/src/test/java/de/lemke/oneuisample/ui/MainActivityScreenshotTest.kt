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
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.viewpager2.widget.ViewPager2
import com.github.takahirom.roborazzi.captureRoboImage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// sdk = [36]: Robolectric 4.16.1 max supported SDK; bump when 4.17+ adds SDK 37.
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainActivityScreenshotTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        ApplicationProvider
            .getApplicationContext<HiltTestApplication>()
            .getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .bypassOobe()
    }

    private fun captureMainScreenshot(
        fileName: String,
        navDestId: Int? = null,
        designSubtabIndex: Int? = null,
    ) {
        val context = ApplicationProvider.getApplicationContext<HiltTestApplication>()
        ActivityScenario
            .launch<MainActivity>(Intent(context, MainActivity::class.java))
            .use { scenario ->
                shadowOf(Looper.getMainLooper()).idle()
                if (navDestId != null) {
                    scenario.onActivity { activity ->
                        (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                            .navController
                            .navigate(navDestId)
                    }
                    shadowOf(Looper.getMainLooper()).idle()
                }
                if (designSubtabIndex != null) {
                    scenario.onActivity { activity ->
                        val navHost = activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
                        navHost.childFragmentManager.primaryNavigationFragment
                            ?.view
                            ?.findViewById<ViewPager2>(R.id.viewPager2Design)
                            ?.setCurrentItem(designSubtabIndex, false)
                    }
                    shadowOf(Looper.getMainLooper()).idle()
                }
                onView(isRoot()).captureRoboImage(fileName)
            }
    }

    // Design tab – Widgets subtab (default)

    @Test
    fun mainActivity_design_light() {
        captureMainScreenshot("src/test/screenshots/main_design_light.png")
    }

    @Test
    @Config(qualifiers = "+night")
    fun mainActivity_design_dark() {
        captureMainScreenshot("src/test/screenshots/main_design_dark.png")
    }

    // Design tab – ProgressBar subtab

    @Test
    fun mainActivity_design_progressBar_light() {
        captureMainScreenshot("src/test/screenshots/main_design_progress_bar_light.png", designSubtabIndex = 1)
    }

    @Test
    @Config(qualifiers = "+night")
    fun mainActivity_design_progressBar_dark() {
        captureMainScreenshot("src/test/screenshots/main_design_progress_bar_dark.png", designSubtabIndex = 1)
    }

    // Design tab – QR subtab

    @Test
    fun mainActivity_design_qr_light() {
        captureMainScreenshot("src/test/screenshots/main_design_qr_light.png", designSubtabIndex = 2)
    }

    @Test
    @Config(qualifiers = "+night")
    fun mainActivity_design_qr_dark() {
        captureMainScreenshot("src/test/screenshots/main_design_qr_dark.png", designSubtabIndex = 2)
    }

    // Picker tab

    @Test
    fun mainActivity_picker_light() {
        captureMainScreenshot("src/test/screenshots/main_picker_light.png", navDestId = R.id.picker_dest)
    }

    @Test
    @Config(qualifiers = "+night")
    fun mainActivity_picker_dark() {
        captureMainScreenshot("src/test/screenshots/main_picker_dark.png", navDestId = R.id.picker_dest)
    }

    // Icons tab

    @Test
    fun mainActivity_icons_light() {
        captureMainScreenshot("src/test/screenshots/main_icons_light.png", navDestId = R.id.icons_dest)
    }

    @Test
    @Config(qualifiers = "+night")
    fun mainActivity_icons_dark() {
        captureMainScreenshot("src/test/screenshots/main_icons_dark.png", navDestId = R.id.icons_dest)
    }
}
