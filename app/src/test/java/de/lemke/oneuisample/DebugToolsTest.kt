package de.lemke.oneuisample

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import leakcanary.AppWatcher
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [36])
class DebugToolsTest {
    @Test
    fun openLeakCanary_startsActivity() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        if (!AppWatcher.isInstalled) {
            AppWatcher.manualInstall(app)
        }
        openLeakCanary(app)
        assertNotNull(shadowOf(app).nextStartedActivity)
    }
}
