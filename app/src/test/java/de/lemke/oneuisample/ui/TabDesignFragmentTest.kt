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
import android.content.Intent
import android.os.Looper
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.HiltTestRule
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabDesignFragmentTest {
    @get:Rule(order = 0)
    val hiltRule = HiltTestRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Inject
    lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    @Test
    fun leavingTheDesignTab_clearsThePagerAdapter() {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            lateinit var pager: ViewPager2
            scenario.onActivity { activity ->
                val designView =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager
                        .primaryNavigationFragment
                        .shouldNotBeNull()
                        .requireView()
                pager = designView.findViewById(R.id.viewPager2Design)
            }
            pager.adapter.shouldNotBeNull()
            scenario.onActivity { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .navController
                    .navigate(R.id.icons_dest)
            }
            shadowOf(Looper.getMainLooper()).idle()
            pager.adapter.shouldBeNull()
        }
    }
}
