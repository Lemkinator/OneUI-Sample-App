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
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class FakeSharedPreferencesTest : ShouldSpec(
    {
        val finishes =
            mapOf<String, (SharedPreferences.Editor) -> Unit>(
                "commit" to { it.commit() },
                "apply" to { it.apply() },
            )

        lateinit var prefs: FakeSharedPreferences
        lateinit var notifiedKeys: MutableList<String?>

        beforeEach {
            prefs = FakeSharedPreferences()
            prefs
                .edit()
                .putString("old", "o")
                .putString("removed", "r")
                .commit()
            notifiedKeys = mutableListOf()
            prefs.registerOnSharedPreferenceChangeListener { _, key -> notifiedKeys += key }
        }

        finishes.forEach { (name, finish) ->
            context("clear() finished by $name") {
                should("keep a put issued before clear()") {
                    finish(prefs.edit().putString("a", "x").clear())

                    prefs.all shouldBe mapOf("a" to "x")
                }

                should("keep a put issued after clear()") {
                    finish(prefs.edit().clear().putString("a", "x"))

                    prefs.all shouldBe mapOf("a" to "x")
                }

                should("re-store a put of a cleared key with its old value") {
                    finish(prefs.edit().putString("old", "o").clear())

                    prefs.all shouldBe mapOf("old" to "o")
                    notifiedKeys shouldBe listOf(null, "old")
                }

                should("apply a remove issued before clear() as a no-op") {
                    finish(
                        prefs
                            .edit()
                            .remove("removed")
                            .clear()
                            .putInt("n", 1),
                    )

                    prefs.all shouldBe mapOf("n" to 1)
                    notifiedKeys shouldBe listOf(null, "n")
                }

                should("notify once with a null key, then once per put") {
                    finish(
                        prefs
                            .edit()
                            .putString("a", "x")
                            .clear()
                            .putBoolean("b", true),
                    )

                    notifiedKeys shouldBe listOf(null, "a", "b")
                }

                should("not replay the clear on a reused editor") {
                    val editor = prefs.edit().clear()
                    finish(editor)
                    finish(editor.putString("a", "x"))

                    prefs.all shouldBe mapOf("a" to "x")
                    notifiedKeys shouldBe listOf(null, "a")
                }
            }
        }
    },
)
