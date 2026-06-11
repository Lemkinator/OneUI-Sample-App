package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.ui.MainActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class TransitionUtilsKtTest {
    @Before
    fun setup() {
        ApplicationProvider
            .getApplicationContext<Application>()
            .getSharedPreferences("user_settings", Context.MODE_PRIVATE)
            .bypassOobe()
    }

    private fun withMainActivity(block: (MainActivity) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Application>()
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity(block)
        }
    }

    @Test
    fun `overrideFadeOpenTransition api34+ branch does not throw`() {
        withMainActivity { it.overrideFadeOpenTransition() }
    }

    @Test
    @Config(application = App::class, sdk = [26])
    fun `overrideFadeOpenTransition legacy branch does not throw`() {
        withMainActivity { it.overrideFadeOpenTransition() }
    }

    @Test
    fun `finishWithFade api34+ branch does not throw`() {
        withMainActivity { it.finishWithFade() }
    }

    @Test
    @Config(application = App::class, sdk = [26])
    fun `finishWithFade legacy branch does not throw`() {
        withMainActivity { it.finishWithFade() }
    }
}
