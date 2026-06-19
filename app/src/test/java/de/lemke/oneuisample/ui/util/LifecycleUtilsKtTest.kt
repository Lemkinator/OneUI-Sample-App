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

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class LifecycleUtilsKtTest : ShouldSpec(
    {

        should("stateInViewModel initial value is returned before upstream emits") {
            val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            val state = flowOf(99).stateInViewModel(scope, 0)
            state.value shouldBe 0
            scope.cancel()
        }

        should("stateInViewModel emits upstream value after subscription") {
            val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            val state = flowOf(42).stateInViewModel(scope, 0)
            state.first { it != 0 } shouldBe 42
            scope.cancel()
        }
    },
)
