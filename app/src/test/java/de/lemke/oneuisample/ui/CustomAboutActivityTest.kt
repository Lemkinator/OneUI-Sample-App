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
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.BackEventCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowActivity

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CustomAboutActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: CustomAboutActivity.() -> Unit = {}) {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun Activity.appBarLayoutAt(
        y: Float,
        top: Int,
    ): AppBarLayout =
        AppBarLayout(this).apply {
            // SESL onMeasure casts its own layout params to CoordinatorLayout.LayoutParams and dereferences them.
            layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, 400)
            addView(View(context), AppBarLayout.LayoutParams(MATCH_PARENT, 200).apply { scrollFlags = SCROLL_FLAG_SCROLL })
            measure(makeMeasureSpec(1000, EXACTLY), makeMeasureSpec(400, EXACTLY))
            layout(0, top, 1000, top + 400)
            this.y = y
            totalScrollRange shouldBe 200
        }

    @Test
    fun onOptionsItemSelected_appInfo_returnsTrue() {
        launch {
            val item = RoboMenuItem(R.id.menu_item_app_info)
            onOptionsItemSelected(item) shouldBe true
        }
    }

    @Test
    fun onOptionsItemSelected_unknown_returnsFalse() {
        launch {
            val item = RoboMenuItem(-1)
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
            val appBarLayout = appBarLayoutAt(y = -200f, top = 0)
            appBarListener.onOffsetChanged(appBarLayout, -200)
            callbackIsActive.value shouldBe true
        }
    }

    @Test
    fun appBarOffsetChanged_expanded_setsCallbackInactive() {
        launch {
            val appBarLayout = appBarLayoutAt(y = 0f, top = 0)
            callbackIsActive.value = true
            appBarListener.onOffsetChanged(appBarLayout, 0)
            callbackIsActive.value shouldBe false
        }
    }

    @Test
    fun appBarOffsetChanged_partial_setsOffsetAlpha() {
        launch {
            val appBarLayout = appBarLayoutAt(y = -20f, top = -20)
            appBarListener.onOffsetChanged(appBarLayout, -20)
            callbackIsActive.value shouldBe false
        }
    }

    @Test
    fun computeBottomAlpha_withPositiveCtlHeight_computesAlpha() {
        launch { computeBottomAlpha(700, 350f) shouldBe (150f / (700 * 0.143f) * (350f - 700 * 0.35f)).coerceIn(0f, 255f) }
    }

    @Test
    fun computeBottomAlpha_withZeroCtlHeight_returnsZero() {
        launch { computeBottomAlpha(0, 350f) shouldBe 0f }
    }

    @Test
    fun updateCallbackState_whenBackProgressing_returnsEarly() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.callbackIsActive.value = true }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
                val appBarLayout = activity.appBarLayoutAt(y = -20f, top = -20)
                activity.appBarListener.onOffsetChanged(appBarLayout, -100)
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
            window.decorView.findViewById<android.view.View>(R.id.aboutCTL)?.layout(0, 0, 1000, 0)
            val appBarLayout = appBarLayoutAt(y = -200f, top = -200)
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
                activity.callbackIsActive.value shouldBe false
            }
        }
    }

    @Test
    fun backGesture_cancelled_collapsesAppBar() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.callbackIsActive.value = true }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val high = BackEventCompat(0f, 0f, 0.6f, BackEventCompat.EDGE_LEFT)
                val low = BackEventCompat(0f, 0f, 0.0f, BackEventCompat.EDGE_LEFT)
                activity.onBackPressedDispatcher.dispatchOnBackStarted(high)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(high)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(low)
                activity.onBackPressedDispatcher.dispatchOnBackCancelled()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.findViewById<AppBarLayout>(R.id.aboutAppBar).seslIsCollapsed() shouldBe true
                activity.callbackIsActive.value shouldBe true
            }
        }
    }

    @Test
    fun backGesture_pressed_expandsAppBarAndDisablesCallback() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.callbackIsActive.value = true }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val high = BackEventCompat(0f, 0f, 0.6f, BackEventCompat.EDGE_LEFT)
                activity.onBackPressedDispatcher.dispatchOnBackStarted(high)
                activity.onBackPressedDispatcher.onBackPressed()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.findViewById<AppBarLayout>(R.id.aboutAppBar).seslIsCollapsed() shouldBe false
                activity.callbackIsActive.value shouldBe false
            }
        }
    }
}
