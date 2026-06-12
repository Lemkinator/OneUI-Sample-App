package de.lemke.oneuisample.benchmarks

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

const val PACKAGE_NAME = "de.lemke.oneuisample"
const val TIMEOUT_MS = 5_000L

// Must match OnboardingUtils.EXTRA_SKIP_ONBOARDING — cannot import from :app
const val EXTRA_SKIP_ONBOARDING = "skipOnboarding"

fun MacrobenchmarkScope.startActivityAndSkipOnboarding() =
    startActivityAndWait { it.putExtra(EXTRA_SKIP_ONBOARDING, true) }

fun UiDevice.flingElementDownUp(element: UiObject2) {
    element.setGestureMargin(displayWidth / 5)
    element.fling(Direction.DOWN)
    waitForIdle()
    element.fling(Direction.UP)
}

fun UiDevice.waitAndFindObject(selector: BySelector, timeout: Long): UiObject2 =
    checkNotNull(wait(Until.findObject(selector), timeout)) {
        "Element not found on screen in ${timeout}ms (selector=$selector)"
    }
