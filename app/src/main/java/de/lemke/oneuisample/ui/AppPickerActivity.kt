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

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.picker.di.AppPickerContext
import androidx.picker.helper.SeslAppInfoDataHelper
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.widget.AppPickerState.OnStateChangeListener
import androidx.picker.widget.SeslAppPickerGridView
import androidx.picker.widget.SeslAppPickerView
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_ASCENDING
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityAppPickerBinding
import de.lemke.oneuisample.ui.util.DEFAULT_LOTTIE_DELAY
import de.lemke.oneuisample.ui.util.ListTypes
import de.lemke.oneuisample.ui.util.collectState
import de.lemke.oneuisample.ui.util.play
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.setEntries
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior.CLEAR_DISMISS
import dev.oneuiproject.oneui.layout.startSearchMode
import dev.oneuiproject.oneui.recyclerview.ktx.seslSetFastScrollerAdditionalPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AppPickerActivity : AppCompatActivity(), ViewYTranslator by AppBarAwareYTranslator() {
    private lateinit var binding: ActivityAppPickerBinding
    private val viewModel: AppPickerViewModel by viewModels()
    private val packageManagerHelper by lazy { AppPickerContext(this).packageManagerHelper }

    @VisibleForTesting(otherwise = PRIVATE)
    internal var currentPicker: SeslAppPickerView? = null
    private var renderedPickerType = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSpinner()
        binding.noEntryView.translateYWithAppBar(binding.toolbarLayout.appBarLayout, this)
        collectState(viewModel.state) { render(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = menuInflater.inflate(R.menu.app_picker, menu).let { true }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_app_picker_search -> {
                binding.toolbarLayout.startSearchMode(
                    onStart = {
                        it.queryHint = getString(R.string.search_apps)
                        binding.appPickerSpinner.isEnabled = false
                    },
                    onQuery = { query, _ ->
                        applyFilter(query)
                        true
                    },
                    onEnd = {
                        applyFilter()
                        binding.appPickerSpinner.isEnabled = true
                    },
                    onBackBehavior = CLEAR_DISMISS,
                )
                true
            }

            else -> {
                false
            }
        }

    private fun initSpinner() {
        binding.appPickerSpinner.apply {
            setEntries(ListTypes.entries.map { getString(it.description) }) { pos, _ ->
                pos?.let { viewModel.onPickerTypeChanged(it) }
            }
            setSelection(viewModel.state.value.pickerType)
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun render(state: AppPickerUiState) {
        if (renderedPickerType == state.pickerType) return
        renderedPickerType = state.pickerType
        setAppPickerType(ListTypes.entries.getOrElse(state.pickerType) { ListTypes.entries.first() })
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onAppItemClick(
        appPicker: SeslAppPickerView,
        appInfo: AppInfo,
    ): Boolean {
        suggestiveSnackBar("${packageManagerHelper.getAppLabel(appInfo)} clicked!")
        return if (appPicker is SeslAppPickerGridView) {
            appPicker.setState(appInfo, !appPicker.getState(appInfo))
            true
        } else {
            false
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onAppItemActionClick(appInfo: AppInfo): Boolean {
        suggestiveSnackBar("${packageManagerHelper.getAppLabel(appInfo)} action clicked!")
        return true
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun applyFilter(query: String = "") {
        currentPicker?.setSearchFilter(query) { updateAppPickerVisibility(it > 0) }
    }

    private fun configureAppPicker(appPicker: SeslAppPickerView) {
        appPicker.apply {
            appListOrder = ORDER_ASCENDING
            seslSetIndexTipEnabled(true)
            seslSetFillHorizontalPaddingEnabled(true)
            seslSetFastScrollerAdditionalPadding(10.dpToPx(resources))
            setOnItemClickEventListener { _, appInfo -> onAppItemClick(this, appInfo) }
            setOnItemActionClickEventListener { _, appInfo -> onAppItemActionClick(appInfo) }
            setOnStateChangeListener(
                object : OnStateChangeListener {
                    @NoCoverage
                    override fun onStateAllChanged(isAllSelected: Boolean) = setStateAll(isAllSelected)

                    @NoCoverage
                    override fun onStateChanged(
                        appInfo: AppInfo,
                        isSelected: Boolean,
                    ) {
                        val allItemsSelected = appPicker.appDataList.filterIsInstance<AppInfoData>().none { !it.selected }
                        (headerFooterAdapter.getItem(0) as? AllAppsViewData)?.selectableItem?.setValueSilence(allItemsSelected)
                    }
                },
            )
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun setAppPickerType(listType: ListTypes) {
        binding.appPickerProgress.isVisible = true
        currentPicker =
            when (listType) {
                ListTypes.TYPE_GRID, ListTypes.TYPE_GRID_CHECKBOX -> {
                    binding.appPickerGrid.also {
                        it.isVisible = true
                        binding.appPickerList.isVisible = false
                    }
                }

                else -> {
                    binding.appPickerList.also {
                        it.isVisible = true
                        binding.appPickerGrid.isVisible = false
                    }
                }
            }
        configureAppPicker(currentPicker!!)
        lifecycleScope.launch(Dispatchers.IO) {
            val packages = getAppList(this@AppPickerActivity, listType)
            withContext(Dispatchers.Main) {
                currentPicker!!.submitList(packages)
                updateAppPickerVisibility(packages.isNotEmpty())
                binding.appPickerProgress.isVisible = false
            }
        }
    }

    fun getAppList(
        context: Context,
        listType: ListTypes,
    ): List<AppInfoData> {
        val actionIcon by lazy { ContextCompat.getDrawable(context, dev.oneuiproject.oneui.R.drawable.ic_oui_settings_outline) }
        return SeslAppInfoDataHelper(context, listType.builder).getPackages().onEach {
            it.subLabel = it.packageName
            if (listType == ListTypes.TYPE_LIST_ACTION_BUTTON) it.actionIcon = actionIcon
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun updateAppPickerVisibility(visible: Boolean) {
        if (visible) {
            binding.noEntryScrollView.isVisible = false
            currentPicker!!.isVisible = true
        } else {
            binding.noEntryScrollView.isVisible = true
            binding.noEntryLottie.play(delay = DEFAULT_LOTTIE_DELAY)
            currentPicker!!.isVisible = false
        }
    }
}
