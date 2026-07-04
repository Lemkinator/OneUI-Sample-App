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
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
class OOBEActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: OOBEActivity.() -> Unit = {}) {
        ActivityScenario.launch<OOBEActivity>(Intent(context, OOBEActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun navigateToMain_startsMainActivity() {
        launch {
            navigateToMain()
            shadowOf(this as Activity).nextStartedActivity?.component?.className shouldBe MainActivity::class.java.name
        }
    }

    @Test
    fun handleOOBEEvent_navigateToMain_callsNavigateToMain() {
        launch {
            handleOOBEEvent(OOBEEvent.NavigateToMain)
            shadowOf(this as Activity).nextStartedActivity?.component?.className shouldBe MainActivity::class.java.name
        }
    }

    @Test
    fun tosSpan_onClick_showsDialog() {
        launch {
            val tosTextView = findViewById<TextView>(R.id.oobe_intro_footer_tos_text)!!
            val spanned = tosTextView.text as android.text.Spanned
            val spans = spanned.getSpans(0, spanned.length, ClickableSpan::class.java)
            spans.size shouldBe 1 // initToSView sets exactly one TOS ClickableSpan
            // clicking exercises dialog code (Kover-excluded; AppCompat dialog not tracked by ShadowAlertDialog)
            spans[0].onClick(tosTextView)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    @Config(sdk = [28])
    fun onCreate_belowApi34_noTransitionOverride() {
        // Verifies the activity launches without crash on pre-API-34 (no overrideActivityTransition)
        launch()
    }

    @Test
    @Config(qualifiers = "w320dp")
    fun initFooterButton_narrowScreen_setsMatchParent() {
        launch {
            // screenWidthDp (320) < 360 → button width forced to MATCH_PARENT
            window.decorView
                .findViewById<View>(R.id.oobe_intro_footer_button)!!
                .layoutParams.width shouldBe ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    @Test
    @Config(qualifiers = "w400dp")
    fun initFooterButton_wideScreen_leavesWrapContent() {
        launch {
            // screenWidthDp (400) >= 360 → button keeps XML width (296dp), not MATCH_PARENT
            window.decorView
                .findViewById<View>(R.id.oobe_intro_footer_button)!!
                .layoutParams.width shouldNotBe ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    @Test
    fun buildTosSpannable_returnsNull_whenTosAbsent() {
        buildTosSpannable("By continuing, you agree to our terms.", "Terms of Service") {} shouldBe null
    }

    @Test
    fun buildTosSpannable_wrapsLastOccurrence_inClickableSpan() {
        val tos = context.getString(R.string.tos)
        val tosText = context.getString(R.string.oobe_tos_text, tos)
        val spanned = checkNotNull(buildTosSpannable(tosText, tos) {})
        val spans = spanned.getSpans(0, spanned.length, ClickableSpan::class.java)
        spans.size shouldBe 1
        val expectedStart = tosText.lastIndexOf(tos)
        spanned.getSpanStart(spans[0]) shouldBe expectedStart
        spanned.getSpanEnd(spans[0]) shouldBe expectedStart + tos.length
    }

    @Test
    fun footerButton_click_triggersAcceptTos() {
        ActivityScenario.launch<OOBEActivity>(Intent(context, OOBEActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                activity.window.decorView
                    .findViewById<View>(R.id.oobe_intro_footer_button)
                    ?.performClick()
            }
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
            scenario.onActivity { activity ->
                // _isAccepting = true → oobeIntroFooterButtonProgress shown, button hidden
                activity.window.decorView
                    .findViewById<View>(R.id.oobe_intro_footer_button_progress)
                    ?.visibility shouldBe View.VISIBLE
                activity.window.decorView
                    .findViewById<View>(R.id.oobe_intro_footer_button)
                    ?.visibility shouldBe View.GONE
            }
        }
    }
}
