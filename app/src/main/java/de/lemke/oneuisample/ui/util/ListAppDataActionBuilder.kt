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
            isValueInSubLabel = false
        )
}
