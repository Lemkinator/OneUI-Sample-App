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
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.UserSettingsSnapshot
import de.lemke.oneuisample.data.withListener
import de.lemke.oneuisample.databinding.DialogSettingsBinding
import de.lemke.oneuisample.databinding.FragmentTabIconsBinding
import de.lemke.oneuisample.domain.Icon
import de.lemke.oneuisample.domain.ObserveIconListUseCase
import de.lemke.oneuisample.ui.util.DEFAULT_LOTTIE_DELAY
import de.lemke.oneuisample.ui.util.IconAdapter
import de.lemke.oneuisample.ui.util.autoCleared
import de.lemke.oneuisample.ui.util.launchAndRepeatWithViewLifecycle
import de.lemke.oneuisample.ui.util.play
import de.lemke.oneuisample.ui.util.showSoftInput
import de.lemke.oneuisample.ui.util.showTipPopup
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.clearBadge
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.hideSoftInput
import dev.oneuiproject.oneui.ktx.setBadge
import dev.oneuiproject.oneui.layout.Badge
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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import dev.oneuiproject.oneui.R as iconsR

@AndroidEntryPoint
class TabIconsFragment : AbsBaseFragment(R.layout.fragment_tab_icons), ViewYTranslator by AppBarAwareYTranslator() {
    private val binding by autoCleared { FragmentTabIconsBinding.bind(requireView()) }
    private lateinit var drawerLayout: DrawerLayout
    private val allSelectorStateFlow: MutableStateFlow<AllSelectorState> = MutableStateFlow(AllSelectorState())

    @VisibleForTesting(otherwise = PRIVATE)
    internal val iconAdapter: IconAdapter by autoCleared {
        IconAdapter(
            requireContext(),
            onAllSelectorStateChanged = { allSelectorStateFlow.value = it },
            onBlockActionMode = ::launchActionMode,
        )
    }

    @Inject
    lateinit var observeIconList: ObserveIconListUseCase

    @Inject
    lateinit var userSettings: UserSettings

    val searchModeListener: ToolbarLayout.SearchModeListener by autoCleared {
        object : ToolbarLayout.SearchModeListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                userSettings.search = query ?: ""
                requireActivity().hideSoftInput()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                userSettings.search = query ?: ""
                return true
            }

            override fun onSearchModeToggle(
                searchView: SearchView,
                isActive: Boolean,
            ) {
                userSettings.searchActive = isActive
                if (isActive) {
                    searchView.setQuery(userSettings.search, false)
                    searchView.showSoftInput()
                    searchView.seslSetOverflowMenuButtonIcon(
                        AppCompatResources.getDrawable(requireContext(), iconsR.drawable.ic_oui_list_filter),
                    )
                    searchView.seslSetOverflowMenuButtonVisibility(VISIBLE)
                    searchView.seslSetOnOverflowMenuButtonClickListener { searchView.onSearchOverflowClicked() }
                }
            }
        }
    }

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
        launchAndRepeatWithViewLifecycle { observeIconList().collectLatest { updateList(it) } }
        launchAndRepeatWithViewLifecycle { userSettings.flow.collectLatest(::applyUserSettings) }
        binding.noEntryView.translateYWithAppBar(requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout).appBarLayout, this)
        showMultiSelectTip()
        binding.fabIcons.setOnClickListener { showFabTip() }
    }

    @NoCoverage
    private fun setupMenuProvider() =
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater,
                ) {
                    menuInflater.inflate(R.menu.icon_tab_menu, menu)
                    menu.findItem(R.id.menu_item_settings)?.setBadge(Badge.DOT)
                }

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
            binding.fabIcons.hideOnScroll(this, binding.iconIndexScroll)
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun updateList(iconsAndSearch: Pair<List<Icon>, String?>) {
        if (iconsAndSearch.first.isEmpty()) {
            binding.iconList.isVisible = false
            binding.noEntryScrollView.isVisible = true
            binding.noEntryLottie.play(delay = DEFAULT_LOTTIE_DELAY)
        } else {
            binding.noEntryScrollView.isVisible = false
            binding.iconList.isVisible = true
            val icons = iconsAndSearch.first
            iconAdapter.highlight = iconsAndSearch.second ?: ""
            iconAdapter.submitList(icons)
            val names = ArrayList<String>(icons.size)
            val indexChars = LinkedHashSet<Char>()
            icons.forEach { icon ->
                names.add(icon.name)
                indexChars.add(icon.indexChar.uppercaseChar())
            }
            val indexCharacterString = indexChars.joinToString("")
            binding.iconIndexScroll.setIndexer(SeslArrayIndexer(names, indexCharacterString))
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

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun applyUserSettings(settings: UserSettingsSnapshot) {
        binding.iconList.seslSetFastScrollerEnabled(!settings.showIndexScroll)
        binding.iconIndexScroll.isVisible = settings.showIndexScroll
        binding.iconIndexScroll.setIndexBarTextMode(settings.indexScrollShowLetters)
        binding.iconIndexScroll.setAutoHide(settings.indexScrollAutoHide)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun isSwipeEnabled(
        @Suppress("UNUSED_PARAMETER") viewHolder: RecyclerView.ViewHolder,
    ): Boolean = !drawerLayout.isActionMode

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onIconSwipeCallback(
        position: Int,
        direction: Int,
        @Suppress("UNUSED_PARAMETER") actionState: Int,
    ): Boolean = onIconSwiped(position, direction)

    private fun configureItemSwipeAnimator() {
        binding.iconList.configureItemSwipeAnimator(
            leftToRightLabel = getString(R.string.left_to_right),
            rightToLeftLabel = getString(R.string.right_to_left),
            leftToRightColor = "#11a85f".toColorInt(),
            rightToLeftColor = "#31a5f3".toColorInt(),
            leftToRightDrawableRes = iconsR.drawable.ic_oui_arrow_right,
            rightToLeftDrawableRes = iconsR.drawable.ic_oui_arrow_left,
            isLeftSwipeEnabled = ::isSwipeEnabled,
            isRightSwipeEnabled = ::isSwipeEnabled,
            onSwiped = ::onIconSwipeCallback,
        )
    }

    private fun startSearch() = drawerLayout.startSearchMode(searchModeListener, DISMISS)

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
        val adapter = iconAdapter
        adapter.toggleActionMode(true, initialSelected)
        drawerLayout.startActionMode(
            onInflateMenu = { menu, menuInflater -> menuInflater.inflate(R.menu.select, menu) },
            onEnd = { adapter.toggleActionMode(false) },
            onSelectMenuItem = ::onActionModeMenuItemSelected,
            onSelectAll = { isChecked: Boolean -> iconAdapter.onToggleSelectAll(isChecked) },
            allSelectorStateFlow = allSelectorStateFlow,
            searchOnActionMode = userSettings.searchOnActionMode.withListener(searchModeListener),
            showCancel = userSettings.actionModeShowCancel,
        )
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onIconTabMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {
            R.id.menu_item_search -> startSearch().let { true }
            R.id.menu_item_settings -> onSettingsMenuItemSelected(menuItem).let { true }
            else -> false
        }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onSettingsMenuItemSelected(menuItem: MenuItem) {
        showSettingsDialog()
        menuItem.clearBadge()
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onActionModeMenuItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_item_1 -> {
                suggestiveSnackBar(getString(R.string.menu_item_1_selected))
                drawerLayout.endActionMode()
                true
            }

            R.id.menu_item_2 -> {
                suggestiveSnackBar(getString(R.string.menu_item_2_selected))
                drawerLayout.endActionMode()
                true
            }

            R.id.menu_item_3 -> {
                suggestiveSnackBar(getString(R.string.menu_item_3_selected))
                drawerLayout.endActionMode()
                true
            }

            else -> {
                false
            }
        }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun buildSettingsDialogView(): DialogSettingsBinding =
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

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun showSettingsDialog() {
        val dialogBinding = buildSettingsDialogView()
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

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun showMultiSelectTip() {
        showTipPopup(
            message = getString(R.string.tip_long_press_multiselect),
            delay = MULTISELECT_TIP_DELAY,
            getAnchor = { binding.iconList.layoutManager?.findViewByPosition(MULTISELECT_TIP_ANCHOR_POSITION) },
        )
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun showFabTip() {
        showTipPopup(
            message = getString(R.string.tip_icons_tab),
            getAnchor = { binding.fabIcons },
        )
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
        userSettings.actionModeShowCancel = actionModeShowCancel
        userSettings.showIndexScroll = showIndexScroll
        userSettings.indexScrollShowLetters = indexScrollShowLetters
        userSettings.indexScrollAutoHide = indexScrollAutoHide
        userSettings.searchOnActionMode =
            when (checkedSearchOnActionModeId) {
                R.id.amsDismiss -> ToolbarLayout.SearchOnActionMode.Dismiss
                R.id.amsNoDismiss -> ToolbarLayout.SearchOnActionMode.NoDismiss
                else -> ToolbarLayout.SearchOnActionMode.Concurrent(null)
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

    companion object {
        private const val MULTISELECT_TIP_ANCHOR_POSITION = 2
        private val MULTISELECT_TIP_DELAY = 1.seconds
    }
}
