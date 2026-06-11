package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class LifecycleUtilsKtFragmentTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withFragment(block: (androidx.fragment.app.Fragment) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val navHost = activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as? NavHostFragment
                val fragment =
                    checkNotNull(navHost?.childFragmentManager?.fragments?.firstOrNull()) {
                        "NavHostFragment contained no fragments"
                    }
                block(fragment)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `Fragment launchAndRepeatWithViewLifecycle executes block when view lifecycle is started`() {
        var executed = false
        withFragment { fragment ->
            fragment.launchAndRepeatWithViewLifecycle { executed = true }
        }
        executed shouldBe true
    }

    @Test
    fun `Fragment collectState delivers initial flow value to callback`() {
        val flow = MutableStateFlow(99)
        var received = -1
        withFragment { fragment ->
            fragment.collectState(flow) { received = it }
        }
        received shouldBe 99
    }

    @Test
    fun `Fragment collectEvents delivers flow emission to callback`() {
        val flow = MutableStateFlow("fragment-event")
        var received = ""
        withFragment { fragment ->
            fragment.collectEvents(flow) { received = it }
        }
        received shouldBe "fragment-event"
    }
}
