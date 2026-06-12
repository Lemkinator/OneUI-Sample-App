package de.lemke.oneuisample.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() =
        rule.collect(
            packageName = PACKAGE_NAME,
            stableIterations = 3,
            maxIterations = 10,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndSkipOnboarding()
        }
}
