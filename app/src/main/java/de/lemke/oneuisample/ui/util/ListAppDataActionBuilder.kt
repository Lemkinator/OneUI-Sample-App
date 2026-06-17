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

import androidx.annotation.Keep
import androidx.picker.model.AppData.AppDataBuilder
import androidx.picker.model.AppData.AppDataBuilderInfo
import androidx.picker.model.AppData.Companion.TYPE_ITEM_ACTION_BUTTON
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.AppInfoDataImpl

@AppDataBuilderInfo(itemType = TYPE_ITEM_ACTION_BUTTON)
@Keep
class ListAppDataActionBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
    override fun build(): AppInfoData =
        AppInfoDataImpl(
            appInfo,
            TYPE_ITEM_ACTION_BUTTON,
            null,
            null,
            null,
            null,
            null,
            null,
            selected = false,
            dimmed = false,
            isValueInSubLabel = false,
        )
}
