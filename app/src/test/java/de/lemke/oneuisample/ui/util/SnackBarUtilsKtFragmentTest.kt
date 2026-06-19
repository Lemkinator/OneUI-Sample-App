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
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import com.google.android.material.R as MaterialR

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class SnackBarUtilsKtFragmentTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withFragment(block: (androidx.fragment.app.Fragment) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val navHost = activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as? NavHostFragment
                val fragment =
                    checkNotNull(navHost?.childFragmentManager?.fragments?.firstOrNull()) {
                        "NavHostFragment contained no fragments"
                    }
                block(fragment)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar String shows snackbar via requireActivity`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar("Test message")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar String with actionText covers non-null let branch`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar("Test message", actionText = "Undo")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar StringRes delegates to String overload`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar(R.string.app_name)
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar StringRes with actionText covers let branch`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar(R.string.ok, actionText = "Dismiss")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar String with explicit action covers non-default path`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar("msg", actionText = "Act", action = { dismiss() })
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar StringRes with explicit action covers non-default path`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar(R.string.app_name, actionText = "Ok", action = { dismiss() })
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar String action button click invokes default dismiss action`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar("msg", actionText = "Dismiss")
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.isShown shouldBe false
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar StringRes action button click invokes default dismiss action`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar(R.string.ok, actionText = "Dismiss")
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)?.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.isShown shouldBe false
        }
    }
}
