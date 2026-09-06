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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import org.junit.Test

class SettingsKeysTest {
    private class Holder(
        prefs: android.content.SharedPreferences,
    ) {
        var alpha: Boolean by prefs.delegates.boolean()
        var beta: Int by prefs.delegates.int()
        val plain: String = "not delegated"
    }

    private open class Base(
        prefs: android.content.SharedPreferences,
    ) {
        var inherited: Boolean by prefs.delegates.boolean()
    }

    private class Sub(
        prefs: android.content.SharedPreferences,
    ) : Base(prefs) {
        var gamma: String by prefs.delegates.string()
    }

    @Test
    fun `passes when the declared delegated properties match exactly`() {
        assertDelegatedKeys(Holder::class.java, setOf("alpha", "beta"))
    }

    @Test
    fun `fails naming a property that is not in the expected set`() {
        val e = shouldThrow<IllegalStateException> { assertDelegatedKeys(Holder::class.java, setOf("alpha")) }
        e.message shouldContain "[beta]"
    }

    @Test
    fun `fails naming an expected key that no property declares`() {
        val e = shouldThrow<IllegalStateException> { assertDelegatedKeys(Holder::class.java, setOf("alpha", "beta", "renamed")) }
        e.message shouldContain "[renamed]"
    }

    @Test
    fun `only counts properties declared on the class itself`() {
        assertDelegatedKeys(Sub::class.java, setOf("gamma"))
    }
}
