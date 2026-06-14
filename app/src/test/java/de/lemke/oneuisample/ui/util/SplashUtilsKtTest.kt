package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.core.splashscreen.SplashScreen
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.MainActivity
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class SplashUtilsKtTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withActivity(block: (MainActivity) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity(block)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `configureSplashScreen with null condition does not call setKeepOnScreenCondition`() {
        withActivity { activity ->
            val splashScreen = mockk<SplashScreen>(relaxed = true)
            val root = mockk<View>(relaxed = true)
            activity.configureSplashScreen(splashScreen, root, condition = null)
            verify(exactly = 0) { splashScreen.setKeepOnScreenCondition(any()) }
        }
    }
}
