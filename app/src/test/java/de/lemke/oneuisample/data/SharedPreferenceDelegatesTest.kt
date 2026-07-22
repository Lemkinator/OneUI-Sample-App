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
package de.lemke.oneuisample.data

import android.content.SharedPreferences
import de.lemke.oneuisample.freshTestPreferences
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchOnActionMode
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SharedPreferenceDelegatesTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var delegates: SharedPreferenceDelegates

    @Before
    fun setup() {
        prefs = freshTestPreferences()
        delegates = SharedPreferenceDelegates(prefs)
    }

    // float

    @Test
    fun `float default returns 0f`() {
        val h =
            object {
                var f: Float by delegates.float(0f)
            }
        h.f shouldBe 0f
    }

    @Test
    fun `float custom default returned before write`() {
        val h =
            object {
                var f: Float by delegates.float(1.5f)
            }
        h.f shouldBe 1.5f
    }

    @Test
    fun `float round-trip`() {
        val h =
            object {
                var f: Float by delegates.float(0f)
            }
        h.f = 3.14f
        h.f shouldBe 3.14f
    }

    // long

    @Test
    fun `long default returns 0L`() {
        val h =
            object {
                var l: Long by delegates.long(0L)
            }
        h.l shouldBe 0L
    }

    @Test
    fun `long round-trip`() {
        val h =
            object {
                var l: Long by delegates.long(0L)
            }
        h.l = 9_999_999_999L
        h.l shouldBe 9_999_999_999L
    }

    // stringSet

    @Test
    fun `stringSet default returns empty set`() {
        val h =
            object {
                var ss: Set<String> by delegates.stringSet()
            }
        h.ss shouldBe emptySet()
    }

    @Test
    fun `stringSet round-trip`() {
        val h =
            object {
                var ss: Set<String> by delegates.stringSet()
            }
        h.ss = setOf("a", "b", "c")
        h.ss shouldBe setOf("a", "b", "c")
    }

    @Test
    fun `stringSet empty set round-trip`() {
        val h =
            object {
                var ss: Set<String> by delegates.stringSet(default = setOf("default"))
            }
        h.ss = emptySet()
        h.ss shouldBe emptySet()
    }

    // darkMode

    @Test
    fun `darkMode default returns false`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(false)
            }
        h.dm shouldBe false
    }

    @Test
    fun `darkMode true round-trip`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(false)
            }
        h.dm = true
        h.dm shouldBe true
    }

    @Test
    fun `darkMode false round-trip`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(true)
            }
        h.dm = false
        h.dm shouldBe false
    }

    @Test
    fun `darkMode stores as string 1 or 0 rather than boolean`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(false)
            }
        h.dm = true
        prefs.getString("dm", null) shouldBe "1"
        h.dm = false
        prefs.getString("dm", null) shouldBe "0"
    }

    // searchOnActionMode

    @Test
    fun `searchOnActionMode default is Dismiss`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode shouldBe SearchOnActionMode.Dismiss
    }

    @Test
    fun `searchOnActionMode round-trip NoDismiss`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode = SearchOnActionMode.NoDismiss
        h.mode shouldBe SearchOnActionMode.NoDismiss
    }

    @Test
    fun `searchOnActionMode round-trip Concurrent`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode = SearchOnActionMode.Concurrent(null)
        h.mode shouldBe SearchOnActionMode.Concurrent(null)
    }

    @Test
    fun `searchOnActionMode round-trip Dismiss after change`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode = SearchOnActionMode.NoDismiss
        h.mode = SearchOnActionMode.Dismiss
        h.mode shouldBe SearchOnActionMode.Dismiss
    }

    // intList

    @Test
    fun `intList default returns empty list`() {
        val h =
            object {
                var list: List<Int> by delegates.intList()
            }
        h.list shouldBe emptyList()
    }

    @Test
    fun `intList round-trip`() {
        val h =
            object {
                var list: List<Int> by delegates.intList(default = emptyList())
            }
        h.list = listOf(4, 5, 6)
        h.list shouldBe listOf(4, 5, 6)
        prefs.getString("list", null) shouldBe "4,5,6"
    }

    @Test
    fun `intList falls back to default after writing an empty list`() {
        val h =
            object {
                var list: List<Int> by delegates.intList(default = listOf(1, 2, 3))
            }
        h.list = emptyList()
        h.list shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `intList returns default when stored value has no parsable ints`() {
        prefs.edit().putString("list", "a,b,c").apply()
        val h =
            object {
                var list: List<Int> by delegates.intList(default = listOf(9))
            }
        h.list shouldBe listOf(9)
    }

    @Test
    fun `intList filters out empty segments produced by consecutive commas`() {
        prefs.edit().putString("list", "1,,2").apply()
        val h =
            object {
                var list: List<Int> by delegates.intList(default = emptyList())
            }
        h.list shouldBe listOf(1, 2)
    }

    @Test
    fun `intList parses negative ints`() {
        prefs.edit().putString("list", "-1,2,-3").apply()
        val h =
            object {
                var list: List<Int> by delegates.intList(default = emptyList())
            }
        h.list shouldBe listOf(-1, 2, -3)
    }

    // sanitized

    @Test
    fun `sanitized clamps a value already out of range in storage on read`() {
        prefs.edit().putInt("size", 9999).apply()
        val h =
            object {
                var size: Int by delegates.int(default = 512).sanitized { it.coerceIn(16, 1024) }
            }
        h.size shouldBe 1024
    }

    @Test
    fun `sanitized clamps on write so the stored value itself is valid`() {
        val h =
            object {
                var size: Int by delegates.int(default = 512).sanitized { it.coerceIn(16, 1024) }
            }
        h.size = -5
        prefs.getInt("size", -1) shouldBe 16
    }

    @Test
    fun `sanitized passes through in-range values unchanged`() {
        val h =
            object {
                var size: Int by delegates.int(default = 512).sanitized { it.coerceIn(16, 1024) }
            }
        h.size = 256
        h.size shouldBe 256
    }

    @Test
    fun `sanitized composes with intList to cap a stored list on read`() {
        prefs.edit().putString("colors", "1,2,3,4,5,6,7,8").apply()
        val h =
            object {
                var colors: List<Int> by delegates.intList(default = emptyList()).sanitized { it.take(6) }
            }
        h.colors shouldBe listOf(1, 2, 3, 4, 5, 6)
    }

    // custom key

    @Test
    fun `custom key overrides property name for storage`() {
        val h =
            object {
                var a: Boolean by delegates.boolean(false, key = "sharedKey")
                var b: Boolean by delegates.boolean(true, key = "sharedKey")
            }
        h.a = true
        h.b shouldBe true
    }

    @Test
    fun `property name used as default key keeps fields independent`() {
        val h =
            object {
                var alpha: Boolean by delegates.boolean(false)
                var beta: Boolean by delegates.boolean(false)
            }
        h.alpha = true
        h.beta shouldBe false
    }

    // no-arg factory calls exercise the Kotlin $default synthetic wrapper's "use default for first param" branch

    @Test
    fun `float no-arg factory uses 0f default`() {
        val h =
            object {
                var f: Float by delegates.float()
            }
        h.f shouldBe 0f
    }

    @Test
    fun `long no-arg factory uses 0L default`() {
        val h =
            object {
                var l: Long by delegates.long()
            }
        h.l shouldBe 0L
    }

    @Test
    fun `boolean no-arg factory uses false default`() {
        val h =
            object {
                var b: Boolean by delegates.boolean()
            }
        h.b shouldBe false
    }

    @Test
    fun `int no-arg factory uses 0 default`() {
        val h =
            object {
                var i: Int by delegates.int()
            }
        h.i shouldBe 0
    }

    @Test
    fun `string no-arg factory uses empty string default`() {
        val h =
            object {
                var s: String by delegates.string()
            }
        h.s shouldBe ""
    }

    @Test
    fun `darkMode no-arg factory uses false default`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode()
            }
        h.dm shouldBe false
    }

    // null-fallback branches: SharedPreferences.getString / getStringSet can return null even
    // with a default argument (e.g. from a mock or a buggy implementation), so the ?: d path
    // in the string() and stringSet() getters must be covered explicitly.

    @Test
    fun `string getter returns default when getString returns null`() {
        val mockPrefs = mockk<SharedPreferences>()
        every { mockPrefs.getString(any(), any()) } returns null
        val d = SharedPreferenceDelegates(mockPrefs)
        val h =
            object {
                var s: String by d.string(default = "fallback")
            }
        h.s shouldBe "fallback"
    }

    @Test
    fun `stringSet getter returns default when getStringSet returns null`() {
        val mockPrefs = mockk<SharedPreferences>()
        every { mockPrefs.getStringSet(any(), any()) } returns null
        val d = SharedPreferenceDelegates(mockPrefs)
        val h =
            object {
                var ss: Set<String> by d.stringSet(default = setOf("x"))
            }
        h.ss shouldBe setOf("x")
    }
}
