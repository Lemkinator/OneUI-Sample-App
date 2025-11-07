package de.lemke.oneuisample.ui.fragments

import android.graphics.ColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.toColorInt
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.SearchOnActionMode
import de.lemke.oneuisample.databinding.DialogSettingsBinding
import de.lemke.oneuisample.databinding.FragmentTabIconsBinding
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.domain.ObserveIconListUseCase
import de.lemke.oneuisample.domain.ObserveUserSettingsUseCase
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.util.IconAdapter
import de.lemke.oneuisample.ui.util.IconAdapter.Icon
import de.lemke.oneuisample.ui.util.IconAdapter.Payload.SELECTION_MODE
import de.lemke.oneuisample.ui.util.autoCleared
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.delegates.AllSelectorState
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.configureItemSwipeAnimator
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.enableCoreSeslFeatures
import dev.oneuiproject.oneui.ktx.hideSoftInput
import dev.oneuiproject.oneui.ktx.hideSoftInputOnScroll
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior.DISMISS
import dev.oneuiproject.oneui.layout.startActionMode
import dev.oneuiproject.oneui.utils.ItemDecorRule.ALL
import dev.oneuiproject.oneui.utils.ItemDecorRule.NONE
import dev.oneuiproject.oneui.utils.SemItemDecoration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.oneuiproject.oneui.R as iconsR

@AndroidEntryPoint
class TabIcons : AbsBaseFragment(R.layout.fragment_tab_icons), ViewYTranslator by AppBarAwareYTranslator() {
    private val binding by autoCleared { FragmentTabIconsBinding.bind(requireView()) }
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var iconAdapter: IconAdapter
    private var isSearchUserInputEnabled = false
    private val allSelectorStateFlow: MutableStateFlow<AllSelectorState> = MutableStateFlow(AllSelectorState())

    @Inject
    lateinit var observeIconList: ObserveIconListUseCase

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var observeUserSettings: ObserveUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.seslSetRefreshOnce(true)
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout)
        initList()
        setupMenuProvider()
        lifecycleScope.launch {
            updateUserSettings { it.copy(searchActive = false) }
            observeIconList().flowWithLifecycle(lifecycle).collectLatest { updateList(it) }
        }
        binding.noEntryView.translateYWithAppBar(requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout).appBarLayout, this)
    }

    private fun setupMenuProvider() = requireActivity().addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) = menuInflater.inflate(R.menu.icon_tab_menu, menu)
        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            R.id.menu_item_search -> startSearch().let { true }
            R.id.menu_item_settings -> showSettingsDialog().let { true }
            else -> false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)


    private fun initList() {
        binding.iconList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = IconAdapter(context, binding.iconIndexScroll).apply { setupOnClickListeners(); iconAdapter = this }
            itemAnimator = null
            addItemDecoration(SemItemDecoration(context, ALL, NONE).apply { setDividerInsetStart(76.dpToPx(resources)) })
            enableCoreSeslFeatures()
            hideSoftInputOnScroll()
            configureItemSwipeAnimator()
        }
        iconAdapter.configure(binding.iconList, SELECTION_MODE, onAllSelectorStateChanged = { allSelectorStateFlow.value = it })
        binding.iconIndexScroll.attachToRecyclerView(binding.iconList)
        lifecycleScope.launch {
            val userSettings = getUserSettings()
            binding.iconList.seslSetFastScrollerEnabled(!userSettings.showIndexScroll)
            binding.iconIndexScroll.isVisible = userSettings.showIndexScroll
            binding.iconIndexScroll.setIndexBarTextMode(userSettings.indexScrollShowLetters)
            binding.iconIndexScroll.setAutoHide(userSettings.indexScrollAutoHide)
        }
    }

    private fun updateList(iconsAndSearch: Pair<List<Icon>, String?>) {
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
        }
    }

    private fun IconAdapter.setupOnClickListeners() {
        onClickItem = { position, icon, _ ->
            if (isActionMode) onToggleItem(icon.id, position)
            else suggestiveSnackBar(icon.beautifiedName, actionText = getString(R.string.ok))
        }
        onLongClickItem = {
            if (!isActionMode) launchActionMode()
            binding.iconList.seslStartLongPressMultiSelection()
        }
    }

    private fun configureItemSwipeAnimator() {
        binding.iconList.configureItemSwipeAnimator(
            leftToRightLabel = "Left to Right",
            rightToLeftLabel = "Right to Left",
            leftToRightColor = "#11a85f".toColorInt(),
            rightToLeftColor = "#31a5f3".toColorInt(),
            leftToRightDrawableRes = iconsR.drawable.ic_oui_arrow_right,
            rightToLeftDrawableRes = iconsR.drawable.ic_oui_arrow_left,
            isLeftSwipeEnabled = { !drawerLayout.isActionMode },
            isRightSwipeEnabled = { !drawerLayout.isActionMode },
            onSwiped = { position, swipeDirection, _ ->
                val icon = iconAdapter.getItemByPosition(position)
                when (swipeDirection) {
                    START -> suggestiveSnackBar("${icon.name}: Right to Left")
                    END -> suggestiveSnackBar("${icon.name}: Left to Right")
                }
                true
            }
        )
    }

    private fun startSearch() = drawerLayout.startSearchMode(searchModeListener, DISMISS)

    val searchModeListener = object : ToolbarLayout.SearchModeListener {
        override fun onQueryTextSubmit(query: String?): Boolean = setSearch(query).also { requireActivity().hideSoftInput() }
        override fun onQueryTextChange(query: String?): Boolean = setSearch(query)
        private fun setSearch(query: String?): Boolean {
            if (!isSearchUserInputEnabled) return false
            lifecycleScope.launch { updateUserSettings { it.copy(search = query ?: "") } }
            return true
        }

        override fun onSearchModeToggle(searchView: SearchView, isActive: Boolean) {
            if (isActive) {
                searchView.apply {
                    seslSetOverflowMenuButtonIcon(AppCompatResources.getDrawable(requireContext(), iconsR.drawable.ic_oui_list_filter))
                    seslSetOverflowMenuButtonVisibility(VISIBLE)
                    seslSetOnOverflowMenuButtonClickListener {
                        clearFocus()
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(getString(R.string.search_filter))
                            setNegativeButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_cancel), null)
                            setPositiveButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_apply), null)
                            show()
                        }
                    }
                }
                isSearchUserInputEnabled = true
                lifecycleScope.launch {
                    updateUserSettings { it.copy(searchActive = true) }
                    searchView.setQuery(getUserSettings().search, false)
                }
            } else {
                isSearchUserInputEnabled = false
                lifecycleScope.launch { updateUserSettings { it.copy(searchActive = false) } }
            }
        }
    }

    private fun launchActionMode(initialSelected: Array<Long>? = null) = lifecycleScope.launch {
        val userSettings = getUserSettings()
        iconAdapter.onToggleActionMode(true, initialSelected)
        drawerLayout.startActionMode(
            onInflateMenu = { menu, menuInflater -> menuInflater.inflate(R.menu.select, menu) },
            onEnd = { iconAdapter.onToggleActionMode(false) },
            onSelectMenuItem = {
                when (it.itemId) {
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

                    else -> false
                }
            },
            onSelectAll = { isChecked: Boolean -> iconAdapter.onToggleSelectAll(isChecked) },
            allSelectorStateFlow = allSelectorStateFlow,
            searchOnActionMode = userSettings.searchOnActionMode.getToolbarValue(searchModeListener),
            showCancel = userSettings.actionModeShowCancel
        )
    }


    private fun showSettingsDialog() {
        lifecycleScope.launch {
            val dialogBinding = DialogSettingsBinding.inflate(layoutInflater).apply {
                getUserSettings().let {
                    actionModeShowCancel.isChecked = it.actionModeShowCancel
                    showIndexScroll.isChecked = it.showIndexScroll
                    indexScrollShowLetters.isChecked = it.indexScrollShowLetters
                    indexScrollAutoHide.isChecked = it.indexScrollAutoHide
                    indexScrollShowLetters.isEnabled = it.showIndexScroll
                    indexScrollAutoHide.isEnabled = it.showIndexScroll
                    when (it.searchOnActionMode) {
                        SearchOnActionMode.DISMISS -> amsOptions.check(R.id.amsDismiss)
                        SearchOnActionMode.NO_DISMISS -> amsOptions.check(R.id.amsNoDismiss)
                        SearchOnActionMode.CONCURRENT -> amsOptions.check(R.id.amsConcurrent)
                    }
                }
                showIndexScroll.onCheckedChangedListener = { _, isChecked ->
                    indexScrollShowLetters.isEnabled = isChecked
                    indexScrollAutoHide.isEnabled = isChecked
                }
            }
            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.settings))
                setView(dialogBinding.root)
                setNegativeButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_cancel), null)
                setPositiveButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_apply)) { _, _ ->
                    lifecycleScope.launch {
                        updateUserSettings {
                            it.copy(
                                actionModeShowCancel = dialogBinding.actionModeShowCancel.isChecked,
                                showIndexScroll = dialogBinding.showIndexScroll.isChecked,
                                indexScrollShowLetters = dialogBinding.indexScrollShowLetters.isChecked,
                                indexScrollAutoHide = dialogBinding.indexScrollAutoHide.isChecked,
                                searchOnActionMode = when (dialogBinding.amsOptions.checkedRadioButtonId) {
                                    R.id.amsDismiss -> SearchOnActionMode.DISMISS
                                    R.id.amsNoDismiss -> SearchOnActionMode.NO_DISMISS
                                    else -> SearchOnActionMode.CONCURRENT
                                }
                            )
                        }.apply {
                            binding.iconList.seslSetFastScrollerEnabled(!showIndexScroll)
                            binding.iconIndexScroll.isVisible = showIndexScroll
                            binding.iconIndexScroll.setIndexBarTextMode(indexScrollShowLetters)
                            binding.iconIndexScroll.setAutoHide(indexScrollAutoHide)
                        }
                    }
                }
                show()
            }
        }
    }
}
