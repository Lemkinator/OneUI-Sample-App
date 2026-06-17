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
import android.text.style.TextAppearanceSpan
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchHighlighterTest {
    private lateinit var highlighter: SearchHighlighter

    @Before
    fun setup() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Application>(), R.style.AppTheme)
        highlighter = SearchHighlighter(context)
    }

    @Test
    fun `returns plain text with no spans when textToBold is null`() {
        val result = highlighter("hello", null)
        result.toString() shouldBe "hello"
        result.getSpans(0, result.length, TextAppearanceSpan::class.java).isEmpty() shouldBe true
    }

    @Test
    fun `returns plain text with no spans when textToBold is blank`() {
        val result = highlighter("hello", "   ")
        result.toString() shouldBe "hello"
        result.getSpans(0, result.length, TextAppearanceSpan::class.java).isEmpty() shouldBe true
    }

    @Test
    fun `sets span on matching section`() {
        val result = highlighter("hello world", "world")
        result.toString() shouldBe "hello world"
        val spans = result.getSpans(6, 11, TextAppearanceSpan::class.java)
        spans.isNotEmpty() shouldBe true
    }

    @Test
    fun `highlighting is case insensitive`() {
        val result = highlighter("Hello World", "WORLD")
        val spans = result.getSpans(6, 11, TextAppearanceSpan::class.java)
        spans.isNotEmpty() shouldBe true
    }

    @Test
    fun `sets spans on all occurrences`() {
        val result = highlighter("star moon star", "star")
        val spans = result.getSpans(0, result.length, TextAppearanceSpan::class.java)
        spans.size shouldBe 2
    }

    @Test
    fun `no spans when text does not contain keyword`() {
        val result = highlighter("hello world", "xyz")
        result.getSpans(0, result.length, TextAppearanceSpan::class.java).isEmpty() shouldBe true
    }

    @Test
    fun `multiple keywords each highlighted`() {
        val result = highlighter("sun and moon", "sun moon")
        val spans = result.getSpans(0, result.length, TextAppearanceSpan::class.java)
        spans.size shouldBe 2
    }

    @Test
    fun `with non-negative lengthBefore truncates prefix when match found far in`() {
        val result = highlighter("aaabbbccc target ddd", "target", 4)
        result.toString().startsWith("...") shouldBe true
        result.toString().contains("target") shouldBe true
    }

    @Test
    fun `with lengthBefore -1 does not truncate prefix`() {
        val result = highlighter("aaabbb target ddd", "target", -1)
        result.toString() shouldBe "aaabbb target ddd"
    }

    @Test
    fun `with lengthBefore 0 and match at position 0 does not truncate`() {
        val result = highlighter("target at start", "target", 0)
        result.toString() shouldBe "target at start"
    }

    @Test
    fun `query with trailing space produces empty token that short-circuits contains check`() {
        // "hello ".split(" ") → ["hello", ""] — the empty token passes text.contains("") but
        // isEmpty()=true in the single-text overload, short-circuiting the contains() call.
        val result = highlighter("hello world", "hello ")
        result.toString() shouldBe "hello world"
        result.getSpans(0, result.length, TextAppearanceSpan::class.java).size shouldBe 1
    }

    @Test
    fun `with non-negative lengthBefore and no match returns builder unchanged`() {
        val result = highlighter("hello", "xyz", 5)
        result.toString() shouldBe "hello"
        result.getSpans(0, result.length, TextAppearanceSpan::class.java).isEmpty() shouldBe true
    }
}
