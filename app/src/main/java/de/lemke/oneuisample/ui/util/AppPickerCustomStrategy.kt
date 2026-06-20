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

import androidx.annotation.Keep
import androidx.picker.features.composable.ComposableFrame
import androidx.picker.features.composable.ComposableType
import androidx.picker.features.composable.ComposableTypeSet
import androidx.picker.features.composable.custom.CustomFrame
import androidx.picker.features.composable.custom.CustomStrategy
import androidx.picker.features.composable.icon.IconFrame
import androidx.picker.features.composable.title.TitleFrame
import androidx.picker.features.composable.widget.WidgetFrame
import androidx.picker.model.AppData.Companion.TYPE_ITEM_ACTION_BUTTON
import androidx.picker.model.AppData.Companion.TYPE_ITEM_CHECKBOX
import androidx.picker.model.AppData.Companion.TYPE_ITEM_RADIOBUTTON
import androidx.picker.model.AppData.Companion.TYPE_ITEM_SWITCH
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import de.lemke.oneuisample.NoCoverage

@Keep
class AppPickerCustomStrategy : CustomStrategy() {
    override fun getCustomFrameList(): List<CustomFrame> = emptyList()

    @NoCoverage
    override fun selectComposableType(viewData: ViewData) =
        if (viewData !is AppInfoViewData) {
            super.selectComposableType(viewData)
        } else {
            when (viewData.itemType) {
                TYPE_ITEM_CHECKBOX -> ComposableTypeSet.CheckBox
                TYPE_ITEM_RADIOBUTTON -> ComposableTypeSet.Radio
                TYPE_ITEM_ACTION_BUTTON -> ActionComposableType()
                TYPE_ITEM_SWITCH -> ComposableTypeSet.Switch
                else -> ComposableTypeSet.TextOnly
            }
        }

    private class ActionComposableType : ComposableType {
        override val leftFrame: ComposableFrame? = null
        override val iconFrame: ComposableFrame = IconFrame.Icon
        override val titleFrame: ComposableFrame = TitleFrame.Title
        override val widgetFrame: ComposableFrame = WidgetFrame.Action
    }
}
