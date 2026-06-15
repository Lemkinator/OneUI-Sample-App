package de.lemke.oneuisample.ui

import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AboutActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: AboutActivity.() -> Unit = {}) {
        ActivityScenario.launch<AboutActivity>(Intent(context, AboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun render_devModeDisabled_showsVersionOnly() {
        launch { render(AboutUiState(devModeEnabled = false)) }
    }

    @Test
    fun render_devModeEnabled_showsDevSuffix() {
        launch { render(AboutUiState(devModeEnabled = true)) }
    }

    @Test
    fun changeStatus_cyclesThroughAllStatuses() {
        launch {
            repeat(9) { changeStatus() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
