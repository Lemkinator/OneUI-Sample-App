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
package de.lemke.oneuisample.ui.util

import android.app.Activity
import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.View
import androidx.reflect.DeviceInfo
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.ui.AboutActivity
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity.IntentForResult

private const val TABLET = "sw600dp-w600dp-h1024dp"
private const val LARGE_PHONE = "sw599dp-w599dp-h1024dp"
private const val ANIM_TYPE_KEY = "android:activity.animType"
private const val ANIM_SCENE_TRANSITION = 5

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = TABLET)
class DrawerPageUtilsTest {
    @After
    fun unmockDeviceInfo() {
        unmockkStatic(DeviceInfo::class)
    }

    private fun oneUi(isOneUi: Boolean) {
        mockkStatic(DeviceInfo::class)
        every { DeviceInfo.isOneUI() } returns isOneUi
    }

    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    private fun Activity.popOverLaunch(): IntentForResult = shadowOf(this).nextStartedActivityForResult.shouldNotBeNull()

    private fun IntentForResult.shouldBePopOver() {
        intent.component?.className shouldBe AboutActivity::class.java.name
        (intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP) shouldBe Intent.FLAG_ACTIVITY_SINGLE_TOP
        options.shouldNotBeNull().getInt(ANIM_TYPE_KEY) shouldNotBe ANIM_SCENE_TRANSITION
    }

    private fun Activity.shouldHaveStartedPlainly() {
        val started = shadowOf(this).nextStartedActivity.shouldNotBeNull()
        started.component?.className shouldBe AboutActivity::class.java.name
        started.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP shouldBe 0
    }

    @Test
    fun `a One UI device at 600dp smallest width opens the page as a pop-over`() {
        oneUi(true)
        val activity = activity()

        activity.launchDrawerPage(AboutActivity::class.java)

        activity.popOverLaunch().shouldBePopOver()
    }

    @Test
    @Config(qualifiers = LARGE_PHONE)
    fun `a One UI device below 600dp smallest width starts the page plainly`() {
        oneUi(true)
        val activity = activity()

        activity.launchDrawerPage(AboutActivity::class.java)

        activity.shouldHaveStartedPlainly()
    }

    @Test
    fun `a large screen without One UI starts the page plainly`() {
        oneUi(false)
        val activity = activity()

        activity.launchDrawerPage(AboutActivity::class.java)

        activity.shouldHaveStartedPlainly()
    }

    @Test
    fun `a view in an activity opens the page as a pop-over on One UI`() {
        oneUi(true)
        val activity = activity()
        val view = View(ContextThemeWrapper(activity, android.R.style.Theme))

        view.launchDrawerPage(AboutActivity::class.java)

        activity.popOverLaunch().shouldBePopOver()
    }

    @Test
    fun `a view outside an activity starts the page as a new task`() {
        oneUi(true)
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        View(context).launchDrawerPage(AboutActivity::class.java)

        val started = shadowOf(context).nextStartedActivity.shouldNotBeNull()
        started.component?.className shouldBe AboutActivity::class.java.name
        (started.flags and Intent.FLAG_ACTIVITY_NEW_TASK) shouldBe Intent.FLAG_ACTIVITY_NEW_TASK
        shadowOf(context).nextStartedActivity.shouldBeNull()
    }
}
