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
import android.content.ContextWrapper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [36])
class SystemServiceUtilsTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun withSystemService_unregisteredService_returnsNullWithoutRunningTheAction() {
        val withoutServices =
            object : ContextWrapper(context) {
                override fun getSystemService(name: String): Any? = null
            }
        var ran = false

        val result = withoutServices.withSystemService(InputMethodManager::class.java) { ran = true }

        result shouldBe null
        ran shouldBe false
    }

    @Test
    fun withSystemService_registeredService_returnsTheActionResult() {
        context.withSystemService(InputMethodManager::class.java) { "ran" } shouldBe "ran"
    }

    @Test
    fun showSoftInput_showsTheKeyboard() {
        val editText = EditText(context)

        editText.showSoftInput()

        shadowOf(context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).isSoftInputVisible shouldBe true
    }
}
