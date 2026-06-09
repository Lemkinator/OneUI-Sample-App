package de.lemke.oneuisample.ui

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import org.junit.Before
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
class MainActivityScreenshotTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userSettings: UserSettingsRepository

    @Before
    fun setup() {
        hiltRule.inject()
        // Bypass OOBE: fresh SharedPreferences has lastVersionCode = -1 which triggers redirect.
        userSettings.lastVersionCode = Int.MAX_VALUE
        userSettings.acceptedTosVersion = Int.MAX_VALUE
        // Bypass OOBE check in TestApp: also set directly on the prefs backing the test instance
        val prefs =
            ApplicationProvider
                .getApplicationContext<HiltTestApplication>()
                .getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        prefs
            .edit()
            .putInt("lastVersionCode", Int.MAX_VALUE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
    }

    private fun captureMainScreenshot(fileName: String) {
        val context = ApplicationProvider.getApplicationContext<HiltTestApplication>()
        ActivityScenario
            .launch<MainActivity>(Intent(context, MainActivity::class.java))
            .use { scenario ->
                shadowOf(Looper.getMainLooper()).idle()
                scenario.onActivity { activity ->
                    activity.window.decorView.captureRoboImage(fileName)
                }
            }
    }

    @Test
    fun mainActivity_light() {
        captureMainScreenshot("src/test/screenshots/main_light.png")
    }

    @Test
    @Config(qualifiers = "+night")
    fun mainActivity_dark() {
        captureMainScreenshot("src/test/screenshots/main_dark.png")
    }
}
