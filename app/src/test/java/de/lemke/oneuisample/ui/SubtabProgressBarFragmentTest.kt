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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.fragments.SubtabProgressBarFragment
import de.lemke.oneuisample.ui.fragments.TabDesignFragment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.concurrent.TimeUnit
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SubtabProgressBarFragmentTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withFragment(block: SubtabProgressBarFragment.() -> Unit) =
        withDesignSubtabFragment(context, PROGRESS_BAR_SUBTAB_INDEX, block)

    @Test
    fun showProgressDialogDemo_showsAndDismissesDialog() {
        withFragment {
            showProgressDialogDemo()
            ShadowDialog.getLatestDialog() shouldNotBe null
            shadowOf(Looper.getMainLooper()).idleFor(5, TimeUnit.SECONDS)
        }
    }

    @Test
    fun showProgressDialogDemo_viewDestroyedMidAnimation_stillDismissesDialog() {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val tabDesignFragment =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager
                        .primaryNavigationFragment as? TabDesignFragment
                tabDesignFragment
                    ?.view
                    ?.findViewById<ViewPager2>(R.id.viewPager2Design)
                    ?.setCurrentItem(PROGRESS_BAR_SUBTAB_INDEX, false)
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val tabDesignFragment =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager
                        .primaryNavigationFragment as? TabDesignFragment
                tabDesignFragment
                    ?.childFragmentManager
                    ?.fragments
                    ?.filterIsInstance<SubtabProgressBarFragment>()
                    ?.firstOrNull()
                    ?.showProgressDialogDemo()
            }
            shadowOf(Looper.getMainLooper()).idle()
            val dialog = ShadowDialog.getLatestDialog()
            dialog shouldNotBe null
            scenario.moveToState(Lifecycle.State.DESTROYED)
            shadowOf(Looper.getMainLooper()).idle()
            dialog?.isShowing shouldBe false
        }
    }

    companion object {
        private const val PROGRESS_BAR_SUBTAB_INDEX = 1
    }
}
