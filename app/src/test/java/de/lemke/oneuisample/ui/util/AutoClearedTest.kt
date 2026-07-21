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
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
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
import io.mockk.every
import io.mockk.mockk
import javax.inject.Inject
import kotlin.reflect.KProperty
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
class AutoClearedTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Inject
    lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    private fun withFragment(block: (Fragment) -> Unit) {
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
    fun `getValue returns cached value on second call without reinitializing`() {
        withFragment { fragment ->
            var initCount = 0
            val delegate = fragment.autoCleared { initCount++ }
            val prop = mockk<KProperty<*>>()
            delegate.getValue(fragment, prop)
            delegate.getValue(fragment, prop)
            initCount shouldBe 1
        }
    }

    @Test
    fun `getValue throws when fragment has no view`() {
        val fragment = Fragment()
        var initCount = 0
        val delegate = fragment.autoCleared { initCount++ }
        val prop = mockk<KProperty<*>>(relaxed = true)
        assertThrows(IllegalStateException::class.java) { delegate.getValue(fragment, prop) }
        initCount shouldBe 0
    }

    @Test
    fun `getValue skips caching when viewLifecycleOwner lifecycle is DESTROYED`() {
        val lifecycle = mockk<Lifecycle>()
        every { lifecycle.currentState } returns Lifecycle.State.DESTROYED
        val lifecycleOwner = mockk<LifecycleOwner>()
        every { lifecycleOwner.lifecycle } returns lifecycle
        val fragment = mockk<Fragment>()
        every { fragment.view } returns mockk<View>()
        every { fragment.viewLifecycleOwner } returns lifecycleOwner
        var initCount = 0
        val delegate = fragment.autoCleared { initCount++ }
        val prop = mockk<KProperty<*>>()
        delegate.getValue(fragment, prop)
        delegate.getValue(fragment, prop)
        initCount shouldBe 2
    }
}
