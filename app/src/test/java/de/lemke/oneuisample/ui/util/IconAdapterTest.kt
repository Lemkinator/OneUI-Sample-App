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
import android.view.ContextThemeWrapper
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [36])
class IconAdapterTest {
    private val themedContext get() = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), R.style.AppTheme)

    private fun adapterWithCounters(): Triple<IconAdapter, IntArray, IntArray> {
        val clicks = IntArray(1)
        val longClicks = IntArray(1)
        val adapter =
            IconAdapter(themedContext, onAllSelectorStateChanged = {}, onBlockActionMode = {}).apply {
                onClickItem = { _, _, _ -> clicks[0]++ }
                onLongClickItem = { longClicks[0]++ }
            }
        return Triple(adapter, clicks, longClicks)
    }

    @Test
    fun unboundHolder_click_callsNoCallback() {
        val (adapter, clicks, _) = adapterWithCounters()
        val holder = adapter.onCreateViewHolder(FrameLayout(themedContext), 0)

        holder.itemView.performClick()

        clicks[0] shouldBe 0
    }

    @Test
    fun unboundHolder_longClick_callsNoCallbackAndConsumesTheEvent() {
        val (adapter, _, longClicks) = adapterWithCounters()
        val holder = adapter.onCreateViewHolder(FrameLayout(themedContext), 0)

        holder.itemView.performLongClick() shouldBe true

        longClicks[0] shouldBe 0
    }
}
