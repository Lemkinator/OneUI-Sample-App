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

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers

/**
 * [TestDispatchersModule] replaces [DispatchersModule] in every `@HiltAndroidTest`, so no
 * Hilt-injected activity/fragment test ever exercises the real bindings. Cover them directly here.
 */
class DispatchersModuleTest : ShouldSpec(
    {
        should("provideIoDispatcher returns Dispatchers.IO") {
            DispatchersModule.provideIoDispatcher() shouldBe Dispatchers.IO
        }

        should("provideDefaultDispatcher returns Dispatchers.Default") {
            DispatchersModule.provideDefaultDispatcher() shouldBe Dispatchers.Default
        }
    },
)
