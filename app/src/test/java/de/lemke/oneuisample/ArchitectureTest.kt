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

import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {
    private val scope = Konsist.scopeFromProduction()

    @Test
    fun `data layer does not depend on ui`() {
        scope.files
            .withPackage("de.lemke.oneuisample.data..")
            .assertFalse { it.hasImport { import -> import.name.startsWith("de.lemke.oneuisample.ui.") } }
    }

    @Test
    fun `domain layer does not depend on ui`() {
        scope.files
            .withPackage("de.lemke.oneuisample.domain..")
            .assertFalse { it.hasImport { import -> import.name.startsWith("de.lemke.oneuisample.ui.") } }
    }

    @Test
    fun `data layer does not depend on domain`() {
        scope.files
            .withPackage("de.lemke.oneuisample.data..")
            .assertFalse { it.hasImport { import -> import.name.startsWith("de.lemke.oneuisample.domain.") } }
    }

    @Test
    fun `companion object is last declaration in class body`() {
        scope
            .classes()
            .assertTrue {
                val companion =
                    it.objects(includeNested = false).lastOrNull { obj ->
                        obj.hasModifier(KoModifier.COMPANION)
                    }
                if (companion != null) {
                    it.declarations(includeNested = false, includeLocal = false).last() == companion
                } else {
                    true
                }
            }
    }
}
