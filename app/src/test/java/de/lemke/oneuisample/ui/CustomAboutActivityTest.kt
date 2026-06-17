package de.lemke.oneuisample.ui

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Looper
import android.view.MenuItem
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.appbar.AppBarLayout
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CustomAboutActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: CustomAboutActivity.() -> Unit = {}) {
        ActivityScenario.launch<CustomAboutActivity>(Intent(context, CustomAboutActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun onOptionsItemSelected_appInfo_returnsTrue() {
        launch {
            val item = mockk<MenuItem> { every { itemId } returns R.id.menu_item_app_info }
            onOptionsItemSelected(item) shouldBe true
        }
    }

    @Test
    fun onOptionsItemSelected_unknown_returnsFalse() {
        launch {
            val item = mockk<MenuItem> { every { itemId } returns -1 }
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
    fun simulateAppBarOffsetChanged_collapsed_enablesBottomContent() {
        launch {
            val appBarLayout =
                mockk<AppBarLayout>(relaxed = true) {
                    every { totalScrollRange } returns 200
                    every { getTotalScrollRange() } returns 200
                    every { y } returns -200f
                    every { top } returns 0
                    every { height } returns 400
                }
            // abs(verticalOffset) >= totalScrollRange / 2 → alpha=0, setBottomContentEnabled(true)
            simulateAppBarOffsetChanged(appBarLayout, -100)
        }
    }

    @Test
    fun simulateAppBarOffsetChanged_expanded_disablesBottomContent() {
        launch {
            val appBarLayout =
                mockk<AppBarLayout>(relaxed = true) {
                    every { totalScrollRange } returns 200
                    every { getTotalScrollRange() } returns 200
                    every { y } returns 0f
                    every { top } returns 0
                    every { height } returns 400
                }
            // abs(verticalOffset) == 0 → alpha=1, setBottomContentEnabled(false)
            simulateAppBarOffsetChanged(appBarLayout, 0)
        }
    }

    @Test
    fun simulateAppBarOffsetChanged_partial_setsOffsetAlpha() {
        launch {
            val appBarLayout =
                mockk<AppBarLayout>(relaxed = true) {
                    every { totalScrollRange } returns 200
                    every { getTotalScrollRange() } returns 200
                    every { y } returns -20f
                    every { top } returns -20
                    every { height } returns 400
                }
            // 0 < abs(verticalOffset) < totalScrollRange / 2 → calculate offset alpha
            simulateAppBarOffsetChanged(appBarLayout, -20)
        }
    }

    @Test
    fun triggerUpdateCallbackState_withExplicitTrue_setsCallbackActive() {
        launch { triggerUpdateCallbackState(true) }
    }

    @Test
    fun triggerUpdateCallbackState_withExplicitFalse_setsCallbackInactive() {
        launch { triggerUpdateCallbackState(false) }
    }

    @Test
    fun triggerUpdateCallbackState_withNull_derivesFromAppBarState() {
        launch { triggerUpdateCallbackState(null) }
    }

    @Test
    fun updateCallbackState_whenBackProgressing_returnsEarly() {
        launch {
            simulateOnBackStarted()
            triggerUpdateCallbackState(true)
        }
    }

    @Test
    fun simulateOnBackProgressed_highProgress_setsExpanding() {
        launch { simulateOnBackProgressed(1.0f) }
    }

    @Test
    fun simulateOnBackProgressed_lowProgressAfterExpanding_collapsesBack() {
        launch {
            simulateOnBackProgressed(1.0f)
            simulateOnBackProgressed(0.0f)
        }
    }

    @Test
    fun simulateOnBackProgressed_midProgress_noStateChange() {
        launch { simulateOnBackProgressed(0.4f) }
    }

    @Test
    fun simulateOnBackProgressed_highProgressWhileExpanding_skipsSetExpanded() {
        launch {
            simulateOnBackProgressed(1.0f)
            simulateOnBackProgressed(1.0f)
        }
    }

    @Test
    fun simulateOnBackProgressed_lowProgressNotExpanding_doesNothing() {
        launch { simulateOnBackProgressed(0.0f) }
    }

    @Test
    fun simulateOnBackPressed_resetsState() {
        launch { simulateOnBackPressed() }
    }

    @Test
    fun simulateOnBackCancelled_resetsState() {
        launch { simulateOnBackCancelled() }
    }

    @Test
    @Config(sdk = [28])
    fun onCreate_belowApi30_noInsetListener() {
        launch { shadowOf(Looper.getMainLooper()).idle() }
    }
}
