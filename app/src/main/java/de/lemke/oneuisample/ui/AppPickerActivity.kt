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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.widget.SeslAppPickerGridView
import androidx.picker.widget.SeslAppPickerView
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_ASCENDING
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityAppPickerBinding
import de.lemke.oneuisample.ui.util.DEFAULT_LOTTIE_DELAY
import de.lemke.oneuisample.ui.util.ListTypes
import de.lemke.oneuisample.ui.util.collectState
import de.lemke.oneuisample.ui.util.play
import de.lemke.oneuisample.ui.util.showSoftInput
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.setEntries
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior.CLEAR_DISMISS
import dev.oneuiproject.oneui.layout.startSearchMode
import dev.oneuiproject.oneui.recyclerview.ktx.seslSetFastScrollerAdditionalPadding

@AndroidEntryPoint
class AppPickerActivity : AppCompatActivity(), ViewYTranslator by AppBarAwareYTranslator() {
    private lateinit var binding: ActivityAppPickerBinding
    private val viewModel: AppPickerViewModel by viewModels()
    private val packageManagerHelper by lazy { AppPickerContext(this).packageManagerHelper }

    private var currentListType = ListTypes.entries.first()

    private val currentPicker: SeslAppPickerView
        get() =
            when (currentListType) {
                ListTypes.TYPE_GRID, ListTypes.TYPE_GRID_CHECKBOX -> binding.appPickerGrid

                ListTypes.LIST_TYPE,
                ListTypes.TYPE_LIST_ACTION_BUTTON,
                ListTypes.TYPE_LIST_CHECKBOX,
                ListTypes.TYPE_LIST_RADIOBUTTON,
                ListTypes.TYPE_LIST_SWITCH,
                -> binding.appPickerList
            }
    private var renderedState: AppPickerUiState? = null
    private var submittedApps: AppListState.Loaded? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSpinner()
        binding.noEntryView.translateYWithAppBar(binding.toolbarLayout.appBarLayout, this)
        collectState(viewModel.state) { render(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_picker, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_app_picker_layout_mode)?.title =
            if (viewModel.state.value.isSelectLayoutMode) {
                getString(R.string.simple_picker_mode)
            } else {
                getString(R.string.select_layout_mode)
            }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_app_picker_search -> {
                binding.toolbarLayout.startSearchMode(
                    onStart = {
                        it.queryHint = getString(R.string.search_apps)
                        it.showSoftInput()
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

            R.id.menu_app_picker_layout_mode -> {
                val isSelectLayoutMode = viewModel.onSelectLayoutModeToggled()
                item.title = getString(if (isSelectLayoutMode) R.string.simple_picker_mode else R.string.select_layout_mode)
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
            setSelection(viewModel.state.value.listType.ordinal)
        }
    }

    private fun render(state: AppPickerUiState) {
        val previous = renderedState
        renderedState = state
        if (previous == null || previous.isSelectLayoutMode != state.isSelectLayoutMode) showAppPickerMode(state.isSelectLayoutMode)
        when (val apps = state.apps) {
            AppListState.Loading -> {
                binding.appPickerProgress.isVisible = true
            }

            is AppListState.Loaded -> {
                if (apps !== submittedApps) {
                    submittedApps = apps
                    showApps(state, apps.apps)
                }
                binding.appPickerProgress.isVisible = false
            }
        }
    }

    private fun showApps(
        state: AppPickerUiState,
        apps: List<AppInfoData>,
    ) {
        if (state.isSelectLayoutMode) {
            binding.appPickerSelectLayout.submitList(apps)
        } else {
            setAppPickerType(state.listType, apps)
        }
    }

    private fun showAppPickerMode(isSelectLayoutMode: Boolean) {
        binding.appPickerSpinner.isVisible = !isSelectLayoutMode
        binding.appPickerSelectLayout.isVisible = isSelectLayoutMode
        if (isSelectLayoutMode) {
            binding.appPickerList.isVisible = false
            binding.appPickerGrid.isVisible = false
            binding.noEntryScrollView.isVisible = false
            configureSelectLayout()
        }
    }

    private fun configureSelectLayout() {
        binding.appPickerProgress.isVisible = true
        binding.appPickerSelectLayout.appPickerStateView.appListOrder = ORDER_ASCENDING
        binding.appPickerSelectLayout.enableSelectedAppPickerView(true)
    }

    private fun onAppItemClick(
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

    private fun onAppItemActionClick(appInfo: AppInfo): Boolean {
        suggestiveSnackBar("${packageManagerHelper.getAppLabel(appInfo)} action clicked!")
        return true
    }

    private fun applyFilter(query: String = "") {
        if (viewModel.state.value.isSelectLayoutMode) {
            binding.appPickerSelectLayout.setSearchFilter(query)
        } else {
            currentPicker.setSearchFilter(query) { updateAppPickerVisibility(it > 0) }
        }
    }

    private fun configureAppPicker(appPicker: SeslAppPickerView) {
        appPicker.apply {
            appListOrder = ORDER_ASCENDING
            seslSetIndexTipEnabled(true)
            seslSetFillHorizontalPaddingEnabled(true)
            seslSetFastScrollerAdditionalPadding(10.dpToPx(resources))
            setOnItemClickEventListener { _, appInfo -> onAppItemClick(this, appInfo) }
            setOnItemActionClickEventListener { _, appInfo -> onAppItemActionClick(appInfo) }
        }
    }

    private fun setAppPickerType(
        listType: ListTypes,
        packages: List<AppInfoData>,
    ) {
        currentListType = listType
        val picker = currentPicker
        binding.appPickerGrid.isVisible = picker === binding.appPickerGrid
        binding.appPickerList.isVisible = picker === binding.appPickerList
        configureAppPicker(picker)
        if (listType == ListTypes.TYPE_LIST_ACTION_BUTTON) {
            packages.forEach {
                it.actionIcon =
                    ResourcesCompat.getDrawable(resources, dev.oneuiproject.oneui.R.drawable.ic_oui_settings_outline, theme)
            }
        }
        picker.submitList(packages)
        updateAppPickerVisibility(packages.isNotEmpty())
    }

    private fun updateAppPickerVisibility(visible: Boolean) {
        if (visible) {
            binding.noEntryScrollView.isVisible = false
            currentPicker.isVisible = true
        } else {
            binding.noEntryScrollView.isVisible = true
            binding.noEntryLottie.play(delay = DEFAULT_LOTTIE_DELAY)
            currentPicker.isVisible = false
        }
    }
}
