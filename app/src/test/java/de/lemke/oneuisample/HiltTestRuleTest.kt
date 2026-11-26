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
package de.lemke.oneuisample

import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The outer rule runs its check after [HiltTestRule] and the test body finished. */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
class HiltTestRuleTest {
    private val closed = mutableListOf<String>()

    @get:Rule(order = 0)
    val assertAfterTest =
        TestRule { base, _ ->
            object : Statement() {
                override fun evaluate() {
                    base.evaluate()
                    closed shouldContainExactly listOf("second", "first")
                }
            }
        }

    @get:Rule(order = 1)
    val hiltRule = HiltTestRule(this)

    @Test
    fun closeAfterTest_closesInReverseOrderAfterTheTest() {
        hiltRule.inject()
        HiltTestRule.closeAfterTest("first") { closed += it }
        HiltTestRule.closeAfterTest("second") { closed += it }
        closed.shouldBeEmpty()
    }
}
