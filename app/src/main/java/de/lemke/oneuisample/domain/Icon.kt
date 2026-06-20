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
package de.lemke.oneuisample.domain

data class Icon(
    val resId: Int,
    val resEntryName: String,
) {
    val id get() = resId.toLong()
    val name get() = resEntryName.removePrefix("ic_oui_")
    val beautifiedName get() = name.replace('_', ' ').replaceFirstChar { it.uppercase() }
    val indexChar get() = name.firstOrNull()?.uppercaseChar() ?: '#'

    fun containsKeywords(keywords: Set<String>): Boolean = keywords.any { name.contains(it, ignoreCase = true) }
}
