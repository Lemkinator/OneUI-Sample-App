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


@Suppress("unused")
@Keep
class AppPickerCustomStrategy : CustomStrategy() {
    override fun getCustomFrameList(): List<CustomFrame> = emptyList()
    override fun selectComposableType(viewData: ViewData) = if (viewData !is AppInfoViewData) super.selectComposableType(viewData)
    else when (viewData.itemType) {
        TYPE_ITEM_CHECKBOX -> ComposableTypeSet.CheckBox
        TYPE_ITEM_RADIOBUTTON -> ComposableTypeSet.Radio
        TYPE_ITEM_ACTION_BUTTON -> ActionComposableType()
        TYPE_ITEM_SWITCH -> ComposableTypeSet.Switch
        else -> ComposableTypeSet.TextOnly
    }

    private class ActionComposableType : ComposableType {
        override val leftFrame: ComposableFrame? = null
        override val iconFrame: ComposableFrame = IconFrame.Icon
        override val titleFrame: ComposableFrame = TitleFrame.Title
        override val widgetFrame: ComposableFrame = WidgetFrame.Action
    }
}