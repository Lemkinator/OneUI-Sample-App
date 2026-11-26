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
package de.lemke.oneuisample.ui

import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.AppInfoDataImpl
import de.lemke.oneuisample.ui.util.AppListSource
import de.lemke.oneuisample.ui.util.ListTypes

/** Returns one fresh app per load, so a reload is visible as new objects. */
class FakeAppListSource : AppListSource {
    val loads = mutableListOf<ListTypes>()

    override fun load(listType: ListTypes): List<AppInfoData> {
        loads += listType
        return listOf(AppInfoDataImpl(AppInfo("app.${listType.name}", "")))
    }
}
