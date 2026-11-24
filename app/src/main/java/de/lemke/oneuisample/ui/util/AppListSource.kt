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

import android.content.Context
import androidx.picker.helper.SeslAppInfoDataHelper
import androidx.picker.model.AppInfoData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

fun interface AppListSource {
    fun load(listType: ListTypes): List<AppInfoData>
}

class InstalledAppListSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppListSource {
    override fun load(listType: ListTypes): List<AppInfoData> =
        SeslAppInfoDataHelper(context, listType.builder).getPackages().onEach { it.subLabel = it.packageName }
}
