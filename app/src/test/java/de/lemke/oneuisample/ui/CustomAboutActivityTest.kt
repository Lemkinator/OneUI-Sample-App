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
import android.graphics.Insets
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.BackEventCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import io.kotest.matchers.floats.plusOrMinus
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

    private fun CustomAboutActivity.appBar() = findViewById<AppBarLayout>(R.id.aboutAppBar)

    private fun CustomAboutActivity.collapseAppBar() {
        appBar().setExpanded(false, false)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun CustomAboutActivity.expandAppBar() {
        appBar().setExpanded(true, false)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun CustomAboutActivity.scrollAppBarBy(dy: Int) {
        val bar = appBar()
        val behavior = (bar.layoutParams as CoordinatorLayout.LayoutParams).behavior as AppBarLayout.Behavior
        behavior.onNestedPreScroll(bar.parent as CoordinatorLayout, bar, bar, 0, dy, IntArray(2), ViewCompat.TYPE_TOUCH)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private val CustomAboutActivity.backCallbackActive get() = onBackPressedDispatcher.hasEnabledCallbacks()

    private val CustomAboutActivity.swipeUpAlpha get() = findViewById<View>(R.id.aboutSwipeUpContainer).alpha

    private val CustomAboutActivity.bottomAlpha get() = findViewById<View>(R.id.aboutBottomContainer).alpha

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
    fun appBar_expandedAtStart_leavesBackCallbackInactive() {
        launch {
            appBar().seslIsCollapsed() shouldBe false
            backCallbackActive shouldBe false
            swipeUpAlpha shouldBe 1f
            bottomAlpha shouldBe 0f
        }
    }

    @Test
    fun appBar_collapsed_activatesBackCallbackAndShowsBottomContent() {
        launch {
            collapseAppBar()
            backCallbackActive shouldBe true
            swipeUpAlpha shouldBe 0f
            bottomAlpha shouldBe 1f
        }
    }

    @Test
    fun appBar_expandedAgain_deactivatesBackCallback() {
        launch {
            collapseAppBar()
            expandAppBar()
            backCallbackActive shouldBe false
            swipeUpAlpha shouldBe 1f
            bottomAlpha shouldBe 0f
        }
    }

    @Test
    fun appBar_scrolledPartway_fadesTheSwipeUpHint() {
        launch {
            scrollAppBarBy(100)
            swipeUpAlpha shouldBe (0.7064579f plusOrMinus ALPHA_TOLERANCE)
            bottomAlpha shouldBe 0f
            backCallbackActive shouldBe false
        }
    }

    @Test
    fun appBar_scrolledPastHalf_hidesTheSwipeUpHintAndFadesInTheBottomContent() {
        launch {
            scrollAppBarBy(600)
            swipeUpAlpha shouldBe 0f
            bottomAlpha shouldBe (0.59834754f plusOrMinus ALPHA_TOLERANCE)
            backCallbackActive shouldBe false
        }
    }

    @Test
    fun appBar_scrolledWhileTheCollapsingLayoutHasNoHeight_keepsTheBottomContentHidden() {
        launch {
            findViewById<View>(R.id.aboutCTL).layout(0, 0, 1000, 0)
            scrollAppBarBy(600)
            bottomAlpha shouldBe 0f
        }
    }

    @Test
    fun configurationChanged_portraitWithCollapsedBar_expandsTheBar() {
        launch {
            collapseAppBar()
            onConfigurationChanged(Configuration(resources.configuration))
            shadowOf(Looper.getMainLooper()).idle()
            appBar().seslIsCollapsed() shouldBe false
            backCallbackActive shouldBe false
        }
    }

    @Test
    @Config(qualifiers = "land")
    fun landscape_collapsesTheBarAndKeepsBackCallbackInactive() {
        launch {
            appBar().seslIsCollapsed() shouldBe true
            backCallbackActive shouldBe false
            bottomAlpha shouldBe 1f
            findViewById<View>(R.id.aboutSwipeUpContainer).isVisible shouldBe false
        }
    }

    @Test
    fun multiWindow_collapsedBar_deactivatesBackCallback() {
        launch {
            collapseAppBar()
            backCallbackActive shouldBe true
            (shadowOf(this as Activity) as ShadowActivity).setInMultiWindowMode(true)
            onConfigurationChanged(Configuration(resources.configuration))
            shadowOf(Looper.getMainLooper()).idle()
            backCallbackActive shouldBe false
        }
    }

    @Test
    fun systemBarInsets_becomeRootPadding() {
        launch {
            val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            root.dispatchApplyWindowInsets(
                WindowInsets.Builder().setInsets(WindowInsets.Type.systemBars(), Insets.of(1, 2, 3, 4)).build(),
            )
            listOf(root.paddingLeft, root.paddingTop, root.paddingRight, root.paddingBottom) shouldBe listOf(1, 2, 3, 4)
        }
    }

    @Test
    @Config(sdk = [28])
    fun onCreate_belowApi30_noInsetListener() {
        launch {
            val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            root.requestApplyInsets()
            shadowOf(Looper.getMainLooper()).idle()
            listOf(root.paddingLeft, root.paddingTop, root.paddingRight, root.paddingBottom) shouldBe listOf(0, 0, 0, 0)
        }
    }

    @Test
    fun backGesture_progressWhileCollapsed_expandsAndCollapsesTheBarAndCancelRestoresIt() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.collapseAppBar() }
            scenario.onActivity { activity ->
                val high = BackEventCompat(0f, 0f, 0.6f, BackEventCompat.EDGE_LEFT)
                val low = BackEventCompat(0f, 0f, 0.0f, BackEventCompat.EDGE_LEFT)
                activity.onBackPressedDispatcher.dispatchOnBackStarted(high)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(low)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(high)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(high)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(low)
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(high)
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.appBar().seslIsCollapsed() shouldBe false
                activity.onBackPressedDispatcher.dispatchOnBackCancelled()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.appBar().seslIsCollapsed() shouldBe true
                activity.backCallbackActive shouldBe true
            }
        }
    }

    @Test
    fun backGesture_started_freezesTheCallbackStateWhileTheBarMoves() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.collapseAppBar() }
            scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
                activity.expandAppBar()
            }
            scenario.onActivity { activity -> activity.backCallbackActive shouldBe true }
        }
    }

    @Test
    fun backGesture_pressed_expandsAppBarAndDisablesCallback() {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.collapseAppBar() }
            scenario.onActivity { activity ->
                val high = BackEventCompat(0f, 0f, 0.6f, BackEventCompat.EDGE_LEFT)
                activity.onBackPressedDispatcher.dispatchOnBackStarted(high)
                activity.onBackPressedDispatcher.onBackPressed()
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.appBar().seslIsCollapsed() shouldBe false
                activity.backCallbackActive shouldBe false
            }
        }
    }

    companion object {
        private const val ALPHA_TOLERANCE = 0.001f
    }
}
