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
package de.lemke.oneuisample

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {
    @Test
    fun `data layer does not depend on ui`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("de.lemke.oneuisample.data..")
            .forEach { file ->
                assertTrue(
                    file.imports.none { it.name.contains("de.lemke.oneuisample.ui.") },
                    "Data file '${file.name}' must not import from ui layer",
                )
            }
    }

    @Test
    fun `domain layer does not depend on ui`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("de.lemke.oneuisample.domain..")
            .forEach { file ->
                assertTrue(
                    file.imports.none { it.name.contains("de.lemke.oneuisample.ui.") },
                    "Domain file '${file.name}' must not import from ui layer",
                )
            }
    }

    @Test
    fun `data layer does not depend on domain`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("de.lemke.oneuisample.data..")
            .forEach { file ->
                assertTrue(
                    file.imports.none { it.name.contains("de.lemke.oneuisample.domain.") },
                    "Data file '${file.name}' must not import from domain layer",
                )
            }
    }
}
