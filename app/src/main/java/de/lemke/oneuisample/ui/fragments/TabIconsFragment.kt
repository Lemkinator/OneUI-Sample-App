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
package de.lemke.oneuisample.ui.fragments

import android.graphics.ColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.toColorInt
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.indexscroll.widget.SeslArrayIndexer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.data.withListener
import de.lemke.oneuisample.databinding.DialogSettingsBinding
import de.lemke.oneuisample.databinding.FragmentTabIconsBinding
import de.lemke.oneuisample.domain.Icon
import de.lemke.oneuisample.domain.ObserveIconListUseCase
import de.lemke.oneuisample.ui.util.IconAdapter
import de.lemke.oneuisample.ui.util.autoCleared
import de.lemke.oneuisample.ui.util.getSearchListener
import de.lemke.oneuisample.ui.util.launchAndRepeatWithViewLifecycle
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout.AllSelectorState
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior.DISMISS
import dev.oneuiproject.oneui.layout.startActionMode
import dev.oneuiproject.oneui.recyclerview.ktx.configureItemSwipeAnimator
import dev.oneuiproject.oneui.recyclerview.ktx.enableCoreSeslFeatures
import dev.oneuiproject.oneui.recyclerview.ktx.hideSoftInputOnScroll
import dev.oneuiproject.oneui.utils.ItemDecorRule.ALL
import dev.oneuiproject.oneui.utils.ItemDecorRule.NONE
import dev.oneuiproject.oneui.utils.SemItemDecoration
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import dev.oneuiproject.oneui.R as iconsR

@AndroidEntryPoint
class TabIconsFragment : AbsBaseFragment(R.layout.fragment_tab_icons), ViewYTranslator by AppBarAwareYTranslator() {
    private val binding by autoCleared { FragmentTabIconsBinding.bind(requireView()) }
    private lateinit var drawerLayout: DrawerLayout
    private val allSelectorStateFlow: MutableStateFlow<AllSelectorState> = MutableStateFlow(AllSelectorState())

    @VisibleForTesting(otherwise = PRIVATE)
    internal val iconAdapter: IconAdapter by lazy {
        IconAdapter(
            requireContext(),
            onAllSelectorStateChanged = { allSelectorStateFlow.value = it },
            onBlockActionMode = ::launchActionMode,
        )
    }

    @Inject
    lateinit var observeIconList: ObserveIconListUseCase

    @Inject
    lateinit var userSettings: UserSettingsRepository

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.seslSetRefreshOnce(true)
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout)
        initList()
        setupMenuProvider()
        userSettings.searchActive = false
        CoroutineSetup().run()
        binding.noEntryView.translateYWithAppBar(requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout).appBarLayout, this)
    }

    @NoCoverage
    private fun setupMenuProvider() =
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater,
                ) = menuInflater.inflate(R.menu.icon_tab_menu, menu)

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean = onIconTabMenuItemSelected(menuItem)
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )

    private fun initList() {
        binding.iconList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = iconAdapter.apply { setupOnClickListeners() }
            itemAnimator = null
            addItemDecoration(SemItemDecoration(context, ALL, NONE).apply { setDividerInsetStart(76.dpToPx(resources)) })
            enableCoreSeslFeatures()
            hideSoftInputOnScroll()
            configureItemSwipeAnimator()
            iconAdapter.configureWith(this)
            binding.iconIndexScroll.attachToRecyclerView(this)
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun updateList(iconsAndSearch: Pair<List<Icon>, String?>) {
        if (iconsAndSearch.first.isEmpty()) {
            binding.iconList.isVisible = false
            binding.noEntryLottie.cancelAnimation()
            binding.noEntryLottie.progress = 0f
            binding.noEntryScrollView.isVisible = true
            val valueCallback = LottieValueCallback<ColorFilter>(SimpleColorFilter(requireContext().getColor(R.color.primary_color_themed)))
            binding.noEntryLottie.addValueCallback(KeyPath("**"), COLOR_FILTER, valueCallback)
            binding.noEntryLottie.postDelayed({ binding.noEntryLottie.playAnimation() }, 400)
        } else {
            binding.noEntryScrollView.isVisible = false
            binding.iconList.isVisible = true
            iconAdapter.highlight = iconsAndSearch.second ?: ""
            iconAdapter.submitList(iconsAndSearch.first)
            val indexCharacterString =
                iconsAndSearch.first
                    .map { it.indexChar }
                    .distinct()
                    .joinToString("")
                    .uppercase()
            binding.iconIndexScroll.setIndexer(SeslArrayIndexer(iconsAndSearch.first.map { it.name }, indexCharacterString))
        }
    }

    private fun IconAdapter.setupOnClickListeners() {
        onClickItem = { position, icon, _ -> onIconItemClicked(position, icon) }
        onLongClickItem = { onIconItemLongClicked() }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onIconItemClicked(
        position: Int,
        icon: Icon,
    ) {
        if (iconAdapter.isActionMode) {
            iconAdapter.toggleItem(icon.id, position)
        } else {
            suggestiveSnackBar(icon.beautifiedName, actionText = getString(R.string.ok))
        }
    }

    @NoCoverage
    internal fun onIconItemLongClicked() {
        if (!iconAdapter.isActionMode) launchActionMode()
        binding.iconList.seslStartLongPressMultiSelection()
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onIconSwiped(
        position: Int,
        swipeDirection: Int,
    ): Boolean {
        val icon = iconAdapter.getItemByPosition(position)
        when (swipeDirection) {
            START -> suggestiveSnackBar("${icon.name}: Right to Left")
            END -> suggestiveSnackBar("${icon.name}: Left to Right")
        }
        return true
    }

    @NoCoverage
    private fun configureItemSwipeAnimator() {
        SwipeAnimatorSetup().configure()
    }

    private inner class SwipeAnimatorSetup {
        fun configure() {
            binding.iconList.configureItemSwipeAnimator(
                leftToRightLabel = "Left to Right",
                rightToLeftLabel = "Right to Left",
                leftToRightColor = "#11a85f".toColorInt(),
                rightToLeftColor = "#31a5f3".toColorInt(),
                leftToRightDrawableRes = iconsR.drawable.ic_oui_arrow_right,
                rightToLeftDrawableRes = iconsR.drawable.ic_oui_arrow_left,
                isLeftSwipeEnabled = { !drawerLayout.isActionMode },
                isRightSwipeEnabled = { !drawerLayout.isActionMode },
                onSwiped = { position, swipeDirection, _ -> onIconSwiped(position, swipeDirection) },
            )
        }
    }

    private fun startSearch() = drawerLayout.startSearchMode(searchModeListener, DISMISS)

    val searchModeListener by lazy {
        getSearchListener(userSettings) {
            seslSetOverflowMenuButtonIcon(AppCompatResources.getDrawable(requireContext(), iconsR.drawable.ic_oui_list_filter))
            seslSetOverflowMenuButtonVisibility(VISIBLE)
            seslSetOnOverflowMenuButtonClickListener { onSearchOverflowClicked() }
        }
    }

    @NoCoverage
    private fun SearchView.onSearchOverflowClicked() {
        clearFocus()
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.search_filter))
            setNegativeButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_cancel), null)
            setPositiveButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_apply), null)
            show()
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun launchActionMode(initialSelected: Set<Long>? = null) {
        ActionModeLauncher(initialSelected).launch()
    }

    private inner class ActionModeLauncher(private val initialSelected: Set<Long>? = null) {
        fun launch() {
            iconAdapter.toggleActionMode(true, initialSelected)
            drawerLayout.startActionMode(
                onInflateMenu = { menu, menuInflater -> menuInflater.inflate(R.menu.select, menu) },
                onEnd = { iconAdapter.toggleActionMode(false) },
                onSelectMenuItem = { onActionModeMenuItemSelected(it) },
                onSelectAll = { isChecked: Boolean -> iconAdapter.onToggleSelectAll(isChecked) },
                allSelectorStateFlow = allSelectorStateFlow,
                searchOnActionMode = userSettings.searchOnActionMode.withListener(searchModeListener),
                showCancel = userSettings.actionModeShowCancel,
            )
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onIconTabMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {
            R.id.menu_item_search -> startSearch().let { true }
            R.id.menu_item_settings -> showSettingsDialog().let { true }
            else -> false
        }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onActionModeMenuItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_item_1 -> {
                suggestiveSnackBar("Menu item 1 selected")
                drawerLayout.endActionMode()
                true
            }

            R.id.menu_item_2 -> {
                suggestiveSnackBar("Menu item 2 selected")
                drawerLayout.endActionMode()
                true
            }

            R.id.menu_item_3 -> {
                suggestiveSnackBar("Menu item 3 selected")
                drawerLayout.endActionMode()
                true
            }

            else -> {
                false
            }
        }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun showSettingsDialog() {
        SettingsDialogSetup().show()
    }

    private inner class SettingsDialogSetup {
        fun show() {
            val dialogBinding =
                DialogSettingsBinding.inflate(layoutInflater).apply {
                    actionModeShowCancel.isChecked = userSettings.actionModeShowCancel
                    showIndexScroll.isChecked = userSettings.showIndexScroll
                    indexScrollShowLetters.isChecked = userSettings.indexScrollShowLetters
                    indexScrollAutoHide.isChecked = userSettings.indexScrollAutoHide
                    indexScrollShowLetters.isEnabled = userSettings.showIndexScroll
                    indexScrollAutoHide.isEnabled = userSettings.showIndexScroll
                    when (userSettings.searchOnActionMode) {
                        ToolbarLayout.SearchOnActionMode.Dismiss -> amsOptions.check(R.id.amsDismiss)
                        ToolbarLayout.SearchOnActionMode.NoDismiss -> amsOptions.check(R.id.amsNoDismiss)
                        is ToolbarLayout.SearchOnActionMode.Concurrent -> amsOptions.check(R.id.amsConcurrent)
                    }
                    showIndexScroll.onCheckedChangedListener = { _, isChecked ->
                        onShowIndexScrollChanged(this, isChecked)
                    }
                }
            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.settings))
                setView(dialogBinding.root)
                setNegativeButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_cancel), null)
                setPositiveButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_apply)) { _, _ ->
                    applySettingsFromDialog(dialogBinding)
                }
                show()
            }
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun applySettingsFromDialog(dialogBinding: DialogSettingsBinding) =
        applySettings(
            actionModeShowCancel = dialogBinding.actionModeShowCancel.isChecked,
            showIndexScroll = dialogBinding.showIndexScroll.isChecked,
            indexScrollShowLetters = dialogBinding.indexScrollShowLetters.isChecked,
            indexScrollAutoHide = dialogBinding.indexScrollAutoHide.isChecked,
            checkedSearchOnActionModeId = dialogBinding.amsOptions.checkedRadioButtonId,
        )

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun applySettings(
        actionModeShowCancel: Boolean,
        showIndexScroll: Boolean,
        indexScrollShowLetters: Boolean,
        indexScrollAutoHide: Boolean,
        checkedSearchOnActionModeId: Int,
    ) {
        userSettings.update {
            copy(
                actionModeShowCancel = actionModeShowCancel,
                showIndexScroll = showIndexScroll,
                indexScrollShowLetters = indexScrollShowLetters,
                indexScrollAutoHide = indexScrollAutoHide,
                searchOnActionMode =
                    when (checkedSearchOnActionModeId) {
                        R.id.amsDismiss -> ToolbarLayout.SearchOnActionMode.Dismiss
                        R.id.amsNoDismiss -> ToolbarLayout.SearchOnActionMode.NoDismiss
                        else -> ToolbarLayout.SearchOnActionMode.Concurrent(null)
                    },
            )
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onShowIndexScrollChanged(
        dialogBinding: DialogSettingsBinding,
        isChecked: Boolean,
    ) {
        dialogBinding.indexScrollShowLetters.isEnabled = isChecked
        dialogBinding.indexScrollAutoHide.isEnabled = isChecked
    }

    private inner class CoroutineSetup {
        fun run() {
            launchAndRepeatWithViewLifecycle { observeIconList().collectLatest { updateList(it) } }
            launchAndRepeatWithViewLifecycle {
                userSettings.flow.collectLatest { settings ->
                    binding.iconList.seslSetFastScrollerEnabled(!settings.showIndexScroll)
                    binding.iconIndexScroll.isVisible = settings.showIndexScroll
                    binding.iconIndexScroll.setIndexBarTextMode(settings.indexScrollShowLetters)
                    binding.iconIndexScroll.setAutoHide(settings.indexScrollAutoHide)
                }
            }
        }
    }
}
