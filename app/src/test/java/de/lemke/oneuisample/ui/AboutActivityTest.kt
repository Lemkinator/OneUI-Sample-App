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
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.BuildConfig.VERSION_NAME
import de.lemke.oneuisample.R
import dev.oneuiproject.oneui.layout.AppInfoLayout
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.Loading
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import dev.oneuiproject.oneui.design.R as designR

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AboutActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: AboutActivity.() -> Unit = {}) {
        ActivityScenario.launch<AboutActivity>(Intent(context, AboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun render_devModeDisabled_showsVersionOnly() {
        launch {
            render(AboutUiState(devModeEnabled = false))
            window.decorView
                .findViewById<TextView>(designR.id.app_info_version)!!
                .text
                .toString()
                .let { text ->
                    text shouldContain VERSION_NAME
                    text shouldNotContain " (dev)"
                }
        }
    }

    @Test
    fun render_devModeEnabled_showsDevSuffix() {
        launch {
            render(AboutUiState(devModeEnabled = true))
            window.decorView
                .findViewById<TextView>(designR.id.app_info_version)!!
                .text
                .toString() shouldContain " (dev)"
        }
    }

    @Test
    fun changeStatus_cyclesThroughAllStatuses() {
        launch {
            // Unset→Loading→UpdateAvailable→UpdateDownloaded→NoUpdate→NotUpdatable→NoConnection→Failed→Unset→Loading
            repeat(9) { changeStatus() }
            shadowOf(Looper.getMainLooper()).idle()
            window.decorView
                .findViewById<AppInfoLayout>(R.id.appInfoLayout)!!
                .updateStatus shouldBe Loading
        }
    }
}
