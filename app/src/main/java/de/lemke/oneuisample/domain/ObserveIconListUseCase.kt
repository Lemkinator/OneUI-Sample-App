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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.data.search
import de.lemke.oneuisample.data.searchActive
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class ObserveIconListUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userSettings: UserSettingsRepository,
) {
    val iconsId = loadIcons()

    @NoCoverage
    private fun loadIcons(): List<Icon> =
        dev.oneuiproject.oneui.R.drawable::class.java.declaredFields
            .filter { it.type == Int::class.java }
            .mapNotNull { field ->
                runCatching {
                    field.isAccessible = true
                    field.getInt(null).let { Icon(it, context.resources.getResourceEntryName(it)) }
                }.getOrNull()
            }

    operator fun invoke() =
        combine(userSettings.flow.search, userSettings.flow.searchActive) { search, active ->
            if (!active || search.isBlank()) {
                iconsId to null
            } else {
                val keywords = search.trim().split(" ").toSet()
                iconsId.filter { it.containsKeywords(keywords) } to search
            }
        }.distinctUntilChanged()
}
