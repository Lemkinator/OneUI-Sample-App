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

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatDelegate
import androidx.picker.model.AppInfoData
import androidx.picker.widget.SeslAppPickerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dagger.hilt.android.testing.HiltAndroidTest
import de.lemke.oneuisample.HiltTestRule
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.util.ListTypes
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ICON_SIZE_PX = 24
private const val ALPHA_SHIFT = 24
private const val OPAQUE_ALPHA = 0xFF
private const val LOAD_TIMEOUT_MILLIS = 15_000L
private const val POLL_MILLIS = 100L
private val NIGHT_ICON_COLOR = 0xFFFAFAFA.toInt()

@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4::class)
class AppPickerNightModeTest {
    @get:Rule(order = 0)
    val hiltRule = HiltTestRule(this)

    @Inject
    lateinit var userSettings: UserSettings

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before
    fun setUp() {
        hiltRule.inject()
        userSettings.bypassOobe()
        userSettings.appPickerType = ListTypes.TYPE_LIST_ACTION_BUTTON.ordinal
    }

    @After
    fun resetNightMode() {
        instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
    }

    @Test
    fun actionIcon_followsTheNightModeOfTheActivityWhileTheApplicationStaysLight() {
        val resumed = CountDownLatch(1)
        val destroyed = CountDownLatch(1)
        val current = AtomicReference<AppPickerActivity?>()
        val callback =
            ActivityLifecycleCallback { activity, stage ->
                if (activity !is AppPickerActivity) return@ActivityLifecycleCallback
                when (stage) {
                    Stage.RESUMED -> {
                        current.set(activity)
                        resumed.countDown()
                    }

                    Stage.DESTROYED -> {
                        current.compareAndSet(activity, null)
                        destroyed.countDown()
                    }

                    else -> {}
                }
            }
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback)
        try {
            instrumentation.targetContext.startActivity(
                Intent(instrumentation.targetContext, AppPickerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            resumed.await(LOAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS) shouldBe true
            instrumentation.runOnMainSync {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                current.get()?.recreate()
            }
            instrumentation.targetContext.isNight() shouldBe false
            var iconColor: Int? = null
            val deadline = System.currentTimeMillis() + LOAD_TIMEOUT_MILLIS
            while (iconColor == null && System.currentTimeMillis() < deadline) {
                instrumentation.runOnMainSync {
                    val activity = current.get()
                    if (activity != null && activity.isNight()) {
                        iconColor = activity.findViewById<SeslAppPickerView>(R.id.appPickerList).actionIconColorOrNull()
                    }
                }
                if (iconColor == null) Thread.sleep(POLL_MILLIS)
            }
            iconColor shouldBe NIGHT_ICON_COLOR
        } finally {
            ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback)
            instrumentation.runOnMainSync { current.get()?.finish() }
            destroyed.await(LOAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }
    }

    private fun Context.isNight() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private fun SeslAppPickerView.actionIconColorOrNull(): Int? {
        val icon = appDataList.filterIsInstance<AppInfoData>().firstOrNull()?.actionIcon ?: return null
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        icon.setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
        icon.draw(Canvas(bitmap))
        return (0 until ICON_SIZE_PX * ICON_SIZE_PX)
            .map { bitmap.getPixel(it % ICON_SIZE_PX, it / ICON_SIZE_PX) }
            .first { it ushr ALPHA_SHIFT == OPAQUE_ALPHA }
    }
}
