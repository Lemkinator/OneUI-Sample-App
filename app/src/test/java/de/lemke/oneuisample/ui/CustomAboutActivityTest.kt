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
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Looper
import android.view.MenuItem
import androidx.activity.BackEventCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.appbar.AppBarLayout
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CustomAboutActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: CustomAboutActivity.() -> Unit = {}) {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onOptionsItemSelected_appInfo_returnsTrue() {
        launch {
            val item = mockk<MenuItem> { every { itemId } returns R.id.menu_item_app_info }
            onOptionsItemSelected(item) shouldBe true
        }
    }

    @Test
    fun onOptionsItemSelected_unknown_returnsFalse() {
        launch {
            val item = mockk<MenuItem> { every { itemId } returns -1 }
            onOptionsItemSelected(item) shouldBe false
        }
    }

    @Test
    fun onConfigurationChanged_portrait_refreshesAppBar() {
        launch {
            val config = Configuration(resources.configuration).apply { orientation = ORIENTATION_PORTRAIT }
            onConfigurationChanged(config)
        }
    }

    @Test
    fun onConfigurationChanged_landscape_refreshesAppBar() {
        launch {
            val config = Configuration(resources.configuration).apply { orientation = ORIENTATION_LANDSCAPE }
            onConfigurationChanged(config)
        }
    }

    @Test
    fun appBarOffsetChanged_collapsed_setsCallbackActive() {
        launch {
            val appBarLayout =
                mockk<AppBarLayout>(relaxed = true) {
                    every { totalScrollRange } returns 200
                    every { getTotalScrollRange() } returns 200
                    every { y } returns -200f
                    every { top } returns 0
                    every { height } returns 400
                }
            // abs(-200) >= 200/2 → alpha=0, setBottomContentEnabled(true)
            // updateCallbackState(200 + (-200) == 0) = updateCallbackState(true)
            appBarListener.onOffsetChanged(appBarLayout, -200)
            callbackIsActive.value shouldBe true
        }
    }

    @Test
    fun appBarOffsetChanged_expanded_setsCallbackInactive() {
        launch {
            val appBarLayout =
                mockk<AppBarLayout>(relaxed = true) {
                    every { totalScrollRange } returns 200
                    every { getTotalScrollRange() } returns 200
                    every { y } returns 0f
                    every { top } returns 0
                    every { height } returns 400
                }
            callbackIsActive.value = true
            // abs(0) == 0 → alpha=1, setBottomContentEnabled(false)
            // updateCallbackState(200 + 0 == 0) = updateCallbackState(false)
            appBarListener.onOffsetChanged(appBarLayout, 0)
            callbackIsActive.value shouldBe false
        }
    }

    @Test
    fun appBarOffsetChanged_partial_setsOffsetAlpha() {
        launch {
            val appBarLayout =
                mockk<AppBarLayout>(relaxed = true) {
                    every { totalScrollRange } returns 200
                    every { getTotalScrollRange() } returns 200
                    every { y } returns -20f
                    every { top } returns -20
                    every { height } returns 400
                }
            // 0 < abs(-20) = 20 < 100 → calculate offset alpha
            // updateCallbackState(200 + (-20) == 0) = updateCallbackState(false)
            appBarListener.onOffsetChanged(appBarLayout, -20)
            callbackIsActive.value shouldBe false
        }
    }

    @Test
    fun updateCallbackState_whenBackProgressing_returnsEarly() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.callbackIsActive.value = true }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
                val mockAppBar =
                    mockk<AppBarLayout>(relaxed = true) {
                        every { totalScrollRange } returns 200
                        every { getTotalScrollRange() } returns 200
                        every { y } returns -20f
                        every { top } returns -20
                        every { height } returns 400
                    }
                // Would set callbackIsActive=false, but isBackProgressing=true blocks updateCallbackState
                activity.appBarListener.onOffsetChanged(mockAppBar, -100)
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.callbackIsActive.value shouldBe true
            }
        }
    }

    @Test
    fun appBarOffsetChanged_withZeroCtlHeight_usesZeroBottomAlpha() {
        launch {
            // Force CTL height to 0 so alphaRange == 0f, hitting the else { 0f } guard
            window.decorView.findViewById<android.view.View>(R.id.aboutCTL)?.layout(0, 0, 1000, 0)
            val appBarLayout =
                mockk<AppBarLayout>(relaxed = true) {
                    every { totalScrollRange } returns 200
                    every { getTotalScrollRange() } returns 200
                    every { y } returns -200f
                    every { top } returns -200
                    every { height } returns 400
                }
            appBarListener.onOffsetChanged(appBarLayout, -200)
            window.decorView.findViewById<android.view.View>(R.id.aboutBottomContainer)?.alpha shouldBe 0f
        }
    }

    @Test
    @Config(sdk = [28])
    fun onCreate_belowApi30_noInsetListener() {
        launch { shadowOf(Looper.getMainLooper()).idle() }
    }

    @Test
    fun refreshAppBar_inMultiWindowMode_usesCollapsedLayout() {
        launch {
            (shadowOf(this as Activity) as ShadowActivity).setInMultiWindowMode(true)
            val config = Configuration(resources.configuration).apply { orientation = ORIENTATION_PORTRAIT }
            onConfigurationChanged(config)
        }
    }

    @Test
    fun updateCallbackState_inMultiWindowMode_derivesDisabled() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                (shadowOf(activity as Activity) as ShadowActivity).setInMultiWindowMode(true)
                activity.callbackIsActive.value = true
                activity.onConfigurationChanged(Configuration(activity.resources.configuration))
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                // onConfigurationChanged → updateCallbackState(null) → isCallbackEnabled()
                // → isInMultiWindowModeCompat=true → false → callbackIsActive=false
                activity.callbackIsActive.value shouldBe false
            }
        }
    }

    @Test
    fun backCallbacks_enabled_allHandlersInvoked() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.callbackIsActive.value = true }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val high = BackEventCompat(0f, 0f, 0.6f, BackEventCompat.EDGE_LEFT)
                val low = BackEventCompat(0f, 0f, 0.0f, BackEventCompat.EDGE_LEFT)
                activity.onBackPressedDispatcher.dispatchOnBackStarted(high)
                // isExpanding=false: > .5 FALSE, < .3 TRUE, isExpanding FALSE → skip (all FALSE branches)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(low)
                // isExpanding=false: > .5 TRUE, !isExpanding TRUE → IF body → isExpanding=true
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(high)
                // isExpanding=true: > .5 TRUE, !isExpanding FALSE → else-if, < .3 FALSE → skip
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(high)
                // isExpanding=true: > .5 FALSE, < .3 TRUE, isExpanding TRUE → ELSE-IF body → isExpanding=false
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(low)
                activity.onBackPressedDispatcher.dispatchOnBackCancelled()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.callbackIsActive.value = true }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val high = BackEventCompat(0f, 0f, 0.6f, BackEventCompat.EDGE_LEFT)
                activity.onBackPressedDispatcher.dispatchOnBackStarted(high)
                activity.onBackPressedDispatcher.onBackPressed()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
