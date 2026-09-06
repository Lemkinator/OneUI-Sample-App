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
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SharedPreferencesDataStoreTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var store: SharedPreferencesDataStore

    @Before
    fun setUp() {
        prefs = freshTestPreferences()
        store = SharedPreferencesDataStore(prefs)
    }

    @Test
    fun `putString writes through to the backing preferences`() {
        store.putString("s", "v")
        prefs.getString("s", null) shouldBe "v"
        store.getString("s", null) shouldBe "v"
    }

    @Test
    fun `putStringSet writes through to the backing preferences`() {
        store.putStringSet("ss", setOf("a", "b"))
        prefs.getStringSet("ss", null)!! shouldContainExactlyInAnyOrder setOf("a", "b")
        store.getStringSet("ss", null)!! shouldContainExactlyInAnyOrder setOf("a", "b")
    }

    @Test
    fun `putInt writes through to the backing preferences`() {
        store.putInt("i", 7)
        prefs.getInt("i", -1) shouldBe 7
        store.getInt("i", -1) shouldBe 7
    }

    @Test
    fun `putLong writes through to the backing preferences`() {
        store.putLong("l", 7L)
        prefs.getLong("l", -1L) shouldBe 7L
        store.getLong("l", -1L) shouldBe 7L
    }

    @Test
    fun `putFloat writes through to the backing preferences`() {
        store.putFloat("f", 1.5f)
        prefs.getFloat("f", -1f) shouldBe 1.5f
        store.getFloat("f", -1f) shouldBe 1.5f
    }

    @Test
    fun `putBoolean writes through to the backing preferences`() {
        store.putBoolean("b", true)
        prefs.getBoolean("b", false).shouldBeTrue()
        store.getBoolean("b", false).shouldBeTrue()
    }

    @Test
    fun `getters return the supplied default when the key is absent`() {
        store.getString("x", "d") shouldBe "d"
        store.getStringSet("x", setOf("d"))!! shouldContainExactlyInAnyOrder setOf("d")
        store.getInt("x", 3) shouldBe 3
        store.getLong("x", 3L) shouldBe 3L
        store.getFloat("x", 3f) shouldBe 3f
        store.getBoolean("x", false).shouldBeFalse()
    }

    @Test
    fun `UserSettings preferenceDataStore targets the settings' own store`() {
        val settings = UserSettings(prefs, CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher()))
        settings.preferenceDataStore().putBoolean("devModeEnabled", true)
        settings.devModeEnabled.shouldBeTrue()
    }
}
