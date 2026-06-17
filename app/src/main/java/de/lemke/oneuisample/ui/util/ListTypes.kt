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

import androidx.annotation.StringRes
import androidx.picker.model.AppData
import androidx.picker.model.AppData.GridAppDataBuilder
import androidx.picker.model.AppData.GridCheckBoxAppDataBuilder
import androidx.picker.model.AppData.ListAppDataBuilder
import androidx.picker.model.AppData.ListCheckBoxAppDataBuilder
import androidx.picker.model.AppData.ListRadioButtonAppDataBuilder
import androidx.picker.model.AppData.ListSwitchAppDataBuilder
import androidx.picker.model.AppInfoData
import de.lemke.oneuisample.R

enum class ListTypes(
    val builder: Class<out AppData.AppDataBuilder<AppInfoData>>,
    @field:StringRes val description: Int,
) {
    LIST_TYPE(ListAppDataBuilder::class.java, R.string.list),

    TYPE_LIST_ACTION_BUTTON(ListAppDataActionBuilder::class.java, R.string.list_action_button),

    TYPE_LIST_CHECKBOX(ListCheckBoxAppDataBuilder::class.java, R.string.list_checkbox),

    TYPE_LIST_RADIOBUTTON(ListRadioButtonAppDataBuilder::class.java, R.string.list_radiobutton),

    TYPE_LIST_SWITCH(ListSwitchAppDataBuilder::class.java, R.string.list_switch),

    TYPE_GRID(GridAppDataBuilder::class.java, R.string.grid),

    TYPE_GRID_CHECKBOX(GridCheckBoxAppDataBuilder::class.java, R.string.grid_checkbox),
}
