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
package de.lemke.oneuisample.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userSettings: UserSettingsRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        // Ensure shouldShowOOBE=false on fresh emulators (SharedPreferences defaults to
        // lastVersionCode=-1 which triggers OOBEActivity, blocking HiltAndroidRule.after()).
        userSettings.lastVersionCode = Int.MAX_VALUE
        userSettings.acceptedTosVersion = Int.MAX_VALUE
    }

    @Test
    fun activityLaunchesWithoutCrash() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // ActivityScenario.launch() uses startActivitySync() which waits for the main looper to
        // go idle. On this emulator the OneUI NavDrawerLayout (Sesl_CTL) keeps posting layout
        // work after onCreate, and ViewRootImpl's sync barrier blocks idle-handler detection
        // for several seconds per frame. ActivityLifecycleMonitorRegistry fires on all lifecycle
        // stage changes without idle-detection, so it is immune to this timing issue.
        val resumedLatch = CountDownLatch(1)
        val destroyedLatch = CountDownLatch(1)
        var launchedActivity: Activity? = null

        val callback =
            ActivityLifecycleCallback { activity, stage ->
                if (activity::class.java == MainActivity::class.java) {
                    when (stage) {
                        Stage.RESUMED -> {
                            launchedActivity = activity
                            resumedLatch.countDown()
                        }

                        Stage.DESTROYED -> {
                            destroyedLatch.countDown()
                        }

                        else -> {}
                    }
                }
            }

        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback)
        try {
            instrumentation.targetContext.startActivity(
                Intent(instrumentation.targetContext, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            resumedLatch.await(15, TimeUnit.SECONDS) shouldBe true
            launchedActivity shouldNotBe null
            instrumentation.runOnMainSync {
                (launchedActivity as LifecycleOwner).lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) shouldBe true
            }
        } finally {
            ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback)
            // Always finish before returning — HiltAndroidRule.after() blocks until all
            // @AndroidEntryPoint activities are destroyed. Runs even if assertions fail.
            launchedActivity?.let { act ->
                instrumentation.runOnMainSync { act.finish() }
                destroyedLatch.await(10, TimeUnit.SECONDS)
            }
        }
    }
}
