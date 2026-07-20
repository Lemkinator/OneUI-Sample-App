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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import com.google.android.material.R as MaterialR

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
class SnackBarUtilsKtActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettings.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withActivity(block: (MainActivity) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity(block)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `suggestiveSnackBar String shows snackbar with default args`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("Test message")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String with actionText covers non-null let branch`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("Test message", actionText = "Undo")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String with explicit view and duration`() {
        withActivity { activity ->
            val view = activity.window.decorView
            val snackbar = activity.suggestiveSnackBar("Test message", view = view, duration = 5000)
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes delegates to String overload`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar(R.string.app_name)
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes with actionText and explicit view`() {
        withActivity { activity ->
            val view = activity.window.decorView
            val snackbar = activity.suggestiveSnackBar(R.string.ok, view = view, actionText = "Dismiss")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String with explicit action covers non-default action path`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("msg", actionText = "Act", action = { })
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes with explicit action covers non-default action path`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar(R.string.app_name, actionText = "Ok", action = { dismiss() })
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String action button click invokes custom action`() {
        withActivity { activity ->
            var actionCalled = false
            val snackbar = activity.suggestiveSnackBar("msg", actionText = "Act", action = { actionCalled = true })
            shadowOf(Looper.getMainLooper()).idle()
            val actionButton = snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)
            actionButton shouldNotBe null
            actionButton!!.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            actionCalled shouldBe true
        }
    }

    @Test
    fun `suggestiveSnackBar String action button click invokes default dismiss action`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("msg", actionText = "Dismiss")
            shadowOf(Looper.getMainLooper()).idle()
            val actionButton = snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)
            actionButton shouldNotBe null
            actionButton!!.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.isShown shouldBe false
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes action button click invokes default dismiss action`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar(R.string.ok, actionText = "Dismiss")
            shadowOf(Looper.getMainLooper()).idle()
            val actionButton = snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)
            actionButton shouldNotBe null
            actionButton!!.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.isShown shouldBe false
        }
    }
}
