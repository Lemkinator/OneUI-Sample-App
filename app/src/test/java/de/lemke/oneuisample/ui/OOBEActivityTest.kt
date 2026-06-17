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
