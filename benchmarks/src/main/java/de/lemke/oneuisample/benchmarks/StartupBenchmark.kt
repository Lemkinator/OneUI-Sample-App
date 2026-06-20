package de.lemke.oneuisample.benchmarks

import androidx.benchmark.macro.BaselineProfileMode.Disable
import androidx.benchmark.macro.BaselineProfileMode.Require
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode.COLD
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupNoCompilation() = startup(CompilationMode.None())

    @Test
    fun startupBaselineProfileDisabled() =
        startup(CompilationMode.Partial(baselineProfileMode = Disable, warmupIterations = 1))

    @Test
    fun startupBaselineProfile() = startup(CompilationMode.Partial(baselineProfileMode = Require))

    @Test
    fun startupFullCompilation() = startup(CompilationMode.Full())

    private fun startup(compilationMode: CompilationMode) =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = BenchmarkMetrics.allMetrics,
            compilationMode = compilationMode,
            iterations = 15,
            startupMode = COLD,
            setupBlock = { pressHome() },
        ) {
            startActivityAndSkipOnboarding()
        }
}
