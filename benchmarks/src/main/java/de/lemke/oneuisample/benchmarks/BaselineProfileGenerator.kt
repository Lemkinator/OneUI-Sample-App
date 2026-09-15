/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.benchmarks

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    // Startup profile drives dex layout optimization, so it must stay limited to the actual
    // cold-start path — a secondary screen here would bloat startup-prof.txt with non-startup code.
    @Test
    fun startup() =
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndSkipOnboarding()
        }

    @Test
    fun generate() =
        rule.collect(
            packageName = PACKAGE_NAME,
            stableIterations = 3,
            maxIterations = 10,
        ) {
            pressHome()
            startActivityAndSkipOnboarding()
            navigateToIconsAndBack()
        }
}

private fun MacrobenchmarkScope.navigateToIconsAndBack() {
    device.findObject(By.text("Icons"))?.click()
    device.waitAndFindObject(
        By.res(PACKAGE_NAME, "iconList").hasDescendant(By.clazz("android.widget.TextView")),
        TIMEOUT_MS,
    )
    device.pressBack()
    device.waitForIdle()
}
