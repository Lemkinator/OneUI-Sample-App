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

import dagger.hilt.android.testing.HiltAndroidRule
import java.util.concurrent.ConcurrentLinkedDeque
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * [HiltAndroidRule] that also closes every resource registered with [closeAfterTest] once the test ends.
 * Resources close in reverse registration order, before Hilt drops its component. A failing close never skips
 * the remaining closes: the test failure, or else the first close failure, is thrown with the others suppressed.
 */
class HiltTestRule(
    testInstance: Any,
) : TestRule {
    private val hiltRule = HiltAndroidRule(testInstance)

    override fun apply(
        base: Statement,
        description: Description,
    ): Statement = hiltRule.apply(closeResourcesAfter(base), description)

    fun inject() = hiltRule.inject()

    companion object {
        private val resources = ConcurrentLinkedDeque<AutoCloseable>()

        /** Calls [close] on [resource] after the running test and returns [resource]. */
        fun <T> closeAfterTest(
            resource: T,
            close: (T) -> Unit,
        ): T {
            resources.addFirst { close(resource) }
            return resource
        }

        private fun closeResourcesAfter(base: Statement): Statement =
            object : Statement() {
                override fun evaluate() {
                    val testFailure = runCatching { base.evaluate() }.exceptionOrNull()
                    val closeFailure = closeRegisteredResources()
                    when {
                        testFailure != null -> {
                            closeFailure?.let(testFailure::addSuppressed)
                            throw testFailure
                        }

                        closeFailure != null -> {
                            throw closeFailure
                        }
                    }
                }
            }

        private fun closeRegisteredResources(): Throwable? =
            generateSequence(resources::pollFirst).fold(null as Throwable?) { failure, resource ->
                val error = runCatching { resource.close() }.exceptionOrNull()
                when {
                    error == null -> failure
                    failure == null -> error
                    else -> failure.also { it.addSuppressed(error) }
                }
            }
    }
}
