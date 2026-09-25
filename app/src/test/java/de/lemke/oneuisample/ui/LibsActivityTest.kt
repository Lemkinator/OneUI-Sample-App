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
import android.app.Application
import android.content.res.Configuration
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.test.core.app.ActivityScenario
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LibsActivityTest {
    // setDefaultNightMode is process-wide static state; any mode but FOLLOW_SYSTEM overrides the night qualifier.
    @Before
    fun setUp() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    @After
    fun tearDown() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    private fun launch(block: (LibsActivity) -> Unit) {
        ActivityScenario.launch(LibsActivity::class.java).use { scenario ->
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
            scenario.onActivity(block)
        }
    }

    @Test
    fun `shows the open source licenses title`() {
        launch { activity ->
            activity.isNightMode() shouldBe false
            activity.texts() shouldContain "Open source licenses"
        }
    }

    @Test
    @Config(qualifiers = "+night")
    fun `shows the open source licenses title in dark mode`() {
        launch { activity ->
            activity.isNightMode() shouldBe true
            activity.texts() shouldContain "Open source licenses"
        }
    }

    @Test
    fun `back button closes the screen`() {
        launch { activity ->
            val backButton =
                activity
                    .semanticsNodes()
                    .filter { it.config.getOrNull(SemanticsProperties.Role) == Role.Button }
                    .minBy { it.boundsInRoot.top }

            backButton.config[SemanticsActions.OnClick].action!!.invoke()

            activity.isFinishing shouldBe true
        }
    }

    private fun Activity.isNightMode() =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private fun Activity.texts() =
        semanticsNodes()
            .flatMap { it.config.getOrNull(SemanticsProperties.Text).orEmpty() }
            .map { it.text }

    private fun Activity.semanticsNodes(): List<SemanticsNode> =
        window.decorView
            .composeRoot()!!
            .semanticsOwner
            .getAllSemanticsNodes(mergingEnabled = true)

    private fun View.composeRoot(): RootForTest? =
        when (this) {
            is RootForTest -> this
            is ViewGroup -> (0 until childCount).firstNotNullOfOrNull { getChildAt(it).composeRoot() }
            else -> null
        }
}
