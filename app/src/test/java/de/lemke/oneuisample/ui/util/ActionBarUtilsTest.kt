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

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class PlainActivity : AppCompatActivity()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ActionBarUtilsTest {
    private fun createActivity(): PlainActivity {
        val controller = Robolectric.buildActivity(PlainActivity::class.java)
        controller.get().setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar)
        return controller.create().get()
    }

    @Test
    fun requireSupportActionBar_toolbarSet_returnsActionBar() {
        val activity = createActivity()
        activity.setSupportActionBar(Toolbar(activity))
        activity.requireSupportActionBar() shouldBe activity.supportActionBar
    }

    @Test
    fun requireSupportActionBar_noActionBarTheme_throwsNamingActivity() {
        val activity = createActivity()
        shouldThrow<IllegalStateException> { activity.requireSupportActionBar() }
            .message shouldBe "PlainActivity has no support action bar"
    }
}
