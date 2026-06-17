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
package de.lemke.oneuisample.ui.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ListTypesTest : ShouldSpec(
    {
        should("all seven enum values are accessible") {
            ListTypes.entries.size shouldBe 7
        }

        should("valueOf returns the correct constant") {
            ListTypes.valueOf("LIST_TYPE") shouldBe ListTypes.LIST_TYPE
        }

        should("each entry has a non-null builder class") {
            ListTypes.entries.forEach { it.builder shouldNotBe null }
        }

        should("each entry has a positive description resource id") {
            ListTypes.entries.forEach { (it.description > 0) shouldBe true }
        }

        should("LIST_TYPE builder is distinct from TYPE_GRID builder") {
            ListTypes.LIST_TYPE.builder shouldNotBe ListTypes.TYPE_GRID.builder
        }

        should("TYPE_LIST_CHECKBOX and TYPE_LIST_RADIOBUTTON have distinct builders") {
            ListTypes.TYPE_LIST_CHECKBOX.builder shouldNotBe ListTypes.TYPE_LIST_RADIOBUTTON.builder
        }
    },
)
