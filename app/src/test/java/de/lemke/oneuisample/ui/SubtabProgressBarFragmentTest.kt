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
import android.app.Dialog
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.fragments.SubtabProgressBarFragment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.concurrent.TimeUnit
import javax.inject.Inject
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

    @Inject
    lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    private fun withFragment(block: SubtabProgressBarFragment.() -> Unit) =
        withDesignSubtabFragment(context, PROGRESS_BAR_SUBTAB_INDEX, block)

    @Test
    fun showProgressDialogDemo_showsAndDismissesDialog() {
        withFragment {
            showProgressDialogDemo()
            val dialog = ShadowDialog.getLatestDialog()
            dialog shouldNotBe null
            shadowOf(Looper.getMainLooper()).idleFor(5, TimeUnit.SECONDS)
            dialog?.isShowing shouldBe false
        }
    }

    @Test
    fun showProgressDialogDemo_viewDestroyedMidAnimation_stillDismissesDialog() {
        var dialog: Dialog? = null
        withDesignSubtabFragment<SubtabProgressBarFragment>(
            context,
            PROGRESS_BAR_SUBTAB_INDEX,
            block = {
                showProgressDialogDemo()
                dialog = ShadowDialog.getLatestDialog()
                dialog shouldNotBe null
            },
            afterBlock = { scenario ->
                scenario.moveToState(Lifecycle.State.DESTROYED)
                shadowOf(Looper.getMainLooper()).idle()
                dialog?.isShowing shouldBe false
            },
        )
    }

    companion object {
        private const val PROGRESS_BAR_SUBTAB_INDEX = 1
    }
}
