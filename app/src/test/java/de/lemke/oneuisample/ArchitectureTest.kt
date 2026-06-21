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
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoInitBlockDeclaration
import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.ShouldSpec

class ArchitectureTest : ShouldSpec() {
    private val codeScope = Konsist.scopeFromProduction()

    init {
        should("data layer does not depend on ui") {
            codeScope.files
                .withPackage("de.lemke.oneuisample.data..")
                .assertFalse(testName = this.testCase.name.toString()) {
                    it.hasImport { import -> import.name.startsWith("de.lemke.oneuisample.ui.") }
                }
        }
        should("domain layer does not depend on ui") {
            codeScope.files
                .withPackage("de.lemke.oneuisample.domain..")
                .assertFalse(testName = this.testCase.name.toString()) {
                    it.hasImport { import -> import.name.startsWith("de.lemke.oneuisample.ui.") }
                }
        }
        should("data layer does not depend on domain") {
            codeScope.files
                .withPackage("de.lemke.oneuisample.data..")
                .assertFalse(testName = this.testCase.name.toString()) {
                    it.hasImport { import -> import.name.startsWith("de.lemke.oneuisample.domain.") }
                }
        }
        should("properties declared before functions in class body") {
            codeScope
                .classes()
                .assertTrue(testName = this.testCase.name.toString()) { koClass ->
                    val declarations = koClass.declarations(includeNested = false, includeLocal = false)
                    val lastPropertyIndex = declarations.indexOfLast { it is KoPropertyDeclaration }
                    val firstFunctionIndex = declarations.indexOfFirst { it is KoFunctionDeclaration }
                    lastPropertyIndex == -1 || firstFunctionIndex == -1 || lastPropertyIndex < firstFunctionIndex
                }
        }
        should("init blocks declared before functions in class body") {
            codeScope
                .classes()
                .assertTrue(testName = this.testCase.name.toString()) { koClass ->
                    val declarations = koClass.declarations(includeNested = false, includeLocal = false)
                    val lastInitIndex = declarations.indexOfLast { it is KoInitBlockDeclaration }
                    val firstFunctionIndex = declarations.indexOfFirst { it is KoFunctionDeclaration }
                    lastInitIndex == -1 || firstFunctionIndex == -1 || lastInitIndex < firstFunctionIndex
                }
        }
        should("override functions declared before non-override functions in class body") {
            codeScope
                .classes()
                .assertTrue(testName = this.testCase.name.toString()) { koClass ->
                    val functions =
                        koClass
                            .declarations(includeNested = false, includeLocal = false)
                            .filterIsInstance<KoFunctionDeclaration>()
                    val lastOverrideIndex = functions.indexOfLast { it.hasModifier(KoModifier.OVERRIDE) }
                    val firstNonOverrideIndex = functions.indexOfFirst { !it.hasModifier(KoModifier.OVERRIDE) }
                    lastOverrideIndex == -1 || firstNonOverrideIndex == -1 || firstNonOverrideIndex > lastOverrideIndex
                }
        }
        should("companion object is last declaration in class body") {
            val testName = this.testCase.name.toString()
            codeScope
                .classes()
                .assertTrue(testName = testName) {
                    val companion =
                        it.objects(includeNested = false).lastOrNull { obj ->
                            obj.hasModifier(KoModifier.COMPANION)
                        }
                    companion == null || it.declarations(includeNested = false, includeLocal = false).last() == companion
                }
            codeScope
                .interfaces()
                .assertTrue(testName = testName) {
                    val companion =
                        it.objects(includeNested = false).lastOrNull { obj ->
                            obj.hasModifier(KoModifier.COMPANION)
                        }
                    companion == null || it.declarations(includeNested = false, includeLocal = false).last() == companion
                }
        }
    }
}
