/*
 * Copyright 2024-2026 Leonard Lemke
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

import android.content.Intent
import android.os.Looper
import androidx.appcompat.widget.SeslSwitchBar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SwitchBarActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun launch(block: SwitchBarActivity.() -> Unit = {}) {
        ActivityScenario.launch<SwitchBarActivity>(Intent(context, SwitchBarActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { it.block() }
            shadowOf(Looper.getMainLooper()).idle()
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun onSwitchChanged_true_updatesViewModel() {
        launch {
            val switchCompat = mockk<androidx.appcompat.widget.SwitchCompat>(relaxed = true)
            onSwitchChanged(switchCompat, true)
        }
    }

    @Test
    fun onSwitchChanged_false_updatesViewModel() {
        launch {
            val switchCompat = mockk<androidx.appcompat.widget.SwitchCompat>(relaxed = true)
            onSwitchChanged(switchCompat, false)
        }
    }
}
