package de.lemke.oneuisample.benchmarks

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollIconListBaselineProfile() = scrollIconList(CompilationMode.Partial())

    private fun scrollIconList(compilationMode: CompilationMode) =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = 10,
            startupMode = StartupMode.WARM,
            setupBlock = {
                pressHome()
                startActivityAndSkipOnboarding()
                device.findObject(By.text("Icons"))?.click()
                device.waitAndFindObject(
                    By.res(PACKAGE_NAME, "iconList").hasDescendant(By.clazz("android.widget.TextView")),
                    TIMEOUT_MS,
                )
            },
        ) {
            val list = checkNotNull(device.findObject(By.res(PACKAGE_NAME, "iconList"))) { "iconList not found" }
            device.flingElementDownUp(list)
        }
}
