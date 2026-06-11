package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
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
class LifecycleUtilsKtActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs
            .edit()
            .putInt("lastVersionCode", Int.MAX_VALUE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
    }

    private fun withActivity(block: (MainActivity) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity(block)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `launchAndRepeatWithLifecycle executes block when activity is started`() {
        var executed = false
        withActivity { activity ->
            activity.launchAndRepeatWithLifecycle { executed = true }
        }
        executed shouldBe true
    }

    @Test
    fun `collectState delivers initial flow value to callback`() {
        val flow = MutableStateFlow(42)
        var received = -1
        withActivity { activity ->
            activity.collectState(flow) { received = it }
        }
        received shouldBe 42
    }

    @Test
    fun `collectEvents delivers flow emission to callback`() {
        val flow = MutableStateFlow("hello")
        var received = ""
        withActivity { activity ->
            activity.collectEvents(flow) { received = it }
        }
        received shouldBe "hello"
    }
}
