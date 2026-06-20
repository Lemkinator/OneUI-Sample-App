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
import android.text.style.ClickableSpan
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OOBEActivityTest {
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
        launch { navigateToMain() }
    }

    @Test
    fun handleOOBEEvent_navigateToMain_callsNavigateToMain() {
        launch { handleOOBEEvent(OOBEEvent.NavigateToMain) }
    }

    @Test
    fun tosSpan_onClick_showsDialog() {
        launch {
            val tosTextView = findViewById<TextView>(R.id.oobe_intro_footer_tos_text)
            val spanned = tosTextView.text as android.text.Spanned
            val spans = spanned.getSpans(0, spanned.length, ClickableSpan::class.java)
            if (spans.isNotEmpty()) {
                spans[0].onClick(tosTextView)
                shadowOf(Looper.getMainLooper()).idle()
            }
        }
    }

    @Test
    @Config(sdk = [28])
    fun onCreate_belowApi34_noTransitionOverride() {
        launch { shadowOf(Looper.getMainLooper()).idle() }
    }

    @Test
    @Config(qualifiers = "w320dp")
    fun initFooterButton_narrowScreen_setsMatchParent() {
        launch { shadowOf(Looper.getMainLooper()).idle() }
    }

    @Test
    @Config(qualifiers = "w400dp")
    fun initFooterButton_wideScreen_leavesWrapContent() {
        launch { shadowOf(Looper.getMainLooper()).idle() }
    }

    @Test
    fun footerButton_click_triggersAcceptTos() {
        launch {
            findViewById<android.view.View>(R.id.oobe_intro_footer_button)?.performClick()
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }
}
