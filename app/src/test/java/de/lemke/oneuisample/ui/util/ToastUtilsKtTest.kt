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
package de.lemke.oneuisample.ui.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ToastUtilsKtTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `Context toast with string shows the message`() {
        context.toast("hello toast")
        ShadowToast.getTextOfLatestToast() shouldBe "hello toast"
    }

    @Test
    fun `Context toast with string res shows a toast`() {
        context.toast(R.string.ok)
        ShadowToast.getLatestToast() shouldNotBe null
    }
}
