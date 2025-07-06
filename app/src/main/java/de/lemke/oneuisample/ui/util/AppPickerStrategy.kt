package de.lemke.oneuisample.ui.util

import androidx.annotation.Keep
import androidx.picker.controller.strategy.AppItemStrategy
import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData


@Suppress("unused")
@Keep
class AppPickerStrategy(appPickerContext: AppPickerContext) : AppItemStrategy(appPickerContext) {
    override fun convert(dataList: List<AppData>, comparator: Comparator<ViewData>?) =
        super.convert(dataList, comparator).onEach { if (it is AppInfoViewData) it.searchable = listOfNotNull(it.label, it.packageName) }
}