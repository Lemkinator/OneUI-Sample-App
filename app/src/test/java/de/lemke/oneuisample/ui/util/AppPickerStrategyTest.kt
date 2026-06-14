package de.lemke.oneuisample.ui.util

import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoDataImpl
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class AppPickerStrategyTest {
    private lateinit var strategy: AppPickerStrategy

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<App>()
        strategy = AppPickerStrategy(AppPickerContext(context))
    }

    @Test
    fun `convert sets searchable to label and packageName`() {
        val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
        val data = AppInfoDataImpl(appInfo, label = "OneUI Sample")
        val results = strategy.convert(listOf(data), null).filterIsInstance<AppInfoViewData>()
        results.first().searchable shouldBe listOf("OneUI Sample", "de.lemke.oneuisample")
    }

    @Test
    fun `convert always includes packageName in searchable`() {
        val appInfo = AppInfo(packageName = "de.lemke.oneuisample", activityName = "")
        val data = AppInfoDataImpl(appInfo, label = null)
        val results = strategy.convert(listOf(data), null).filterIsInstance<AppInfoViewData>()
        results.first().searchable shouldContain "de.lemke.oneuisample"
    }
}
