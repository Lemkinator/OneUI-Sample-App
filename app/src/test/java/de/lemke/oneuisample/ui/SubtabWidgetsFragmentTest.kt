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

import android.app.Application
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.SeslSwitchBar
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.HiltTestRule
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.fragments.SubtabWidgetsFragment
import de.lemke.oneuisample.ui.fragments.TabDesignFragment
import io.kotest.matchers.shouldBe
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.junit.Before
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
class SubtabWidgetsFragmentTest {
    @get:Rule(order = 0)
    val hiltRule = HiltTestRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Inject
    lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    private fun withFragment(block: SubtabWidgetsFragment.() -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val tabDesignFragment =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager
                        .primaryNavigationFragment as? TabDesignFragment
                val fragment =
                    tabDesignFragment
                        ?.childFragmentManager
                        ?.fragments
                        ?.filterIsInstance<SubtabWidgetsFragment>()
                        ?.firstOrNull()
                fragment?.block()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun SubtabWidgetsFragment.switchBar() = requireView().findViewById<SeslSwitchBar>(R.id.switchBar)

    private fun SeslSwitchBar.progress() = findViewById<View>(androidx.appcompat.R.id.sesl_switchbar_progress)

    @Test
    fun switchBar_click_showsProgressUntilDelayElapsed() {
        withFragment {
            val progress = switchBar().progress()
            switchBar().performClick()
            progress.visibility shouldBe View.VISIBLE
            shadowOf(Looper.getMainLooper()).idleFor(2, TimeUnit.SECONDS)
            progress.visibility shouldBe View.GONE
        }
    }

    @Test
    fun switchBar_secondClickWithinDelay_restartsTheDelay() {
        withFragment {
            val progress = switchBar().progress()
            switchBar().performClick()
            shadowOf(Looper.getMainLooper()).idleFor(600, TimeUnit.MILLISECONDS)
            switchBar().performClick()
            shadowOf(Looper.getMainLooper()).idleFor(600, TimeUnit.MILLISECONDS)
            progress.visibility shouldBe View.VISIBLE
            shadowOf(Looper.getMainLooper()).idleFor(600, TimeUnit.MILLISECONDS)
            progress.visibility shouldBe View.GONE
        }
    }
}
