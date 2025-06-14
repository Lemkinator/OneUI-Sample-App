package de.lemke.oneuisample.ui.fragments

import android.graphics.ColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.toColorInt
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.SearchOnActionMode
import de.lemke.oneuisample.databinding.FragmentTabIconsBinding
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.domain.ObserveIconListUseCase
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.dialog.SearchFilterBottomSheet
import de.lemke.oneuisample.ui.util.IconAdapter
import de.lemke.oneuisample.ui.util.IconAdapter.Icon
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.utils.ItemDecorRule
import dev.oneuiproject.oneui.utils.SemItemDecoration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.oneuiproject.oneui.R as iconsR

@AndroidEntryPoint
class TabIcons : Fragment(), ViewYTranslator by AppBarAwareYTranslator() {
    private lateinit var binding: FragmentTabIconsBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var iconAdapter: IconAdapter
    private var isSearchUserInputEnabled = false
    private val allSelectorStateFlow: MutableStateFlow<AllSelectorState> = MutableStateFlow(AllSelectorState())

    @Inject
    lateinit var observeIconList: ObserveIconListUseCase

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabIconsBinding.inflate(inflater, container, false)
        return binding.root
    }

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
        override fun onPrepareMenu(menu: Menu) {
            super.onPrepareMenu(menu)
            lifecycleScope.launch {
                val userSettings = getUserSettings()
                menu.findItem(R.id.menu_item_show_index_scroll).title =
                    getString(if (userSettings.showIndexScroll) R.string.show_fast_scroller else R.string.show_index_scroll)
                menu.findItem(R.id.menu_item_index_scroll_letters).title =
                    getString(if (userSettings.indexScrollShowLetters) R.string.hide_letters else R.string.show_letters)
                menu.findItem(R.id.menu_item_index_scroll_auto_hide).title =
                    getString(if (userSettings.indexScrollAutoHide) R.string.disable_auto_hide else R.string.enable_auto_hide)
                menu.findItem(R.id.menu_item_show_cancel).title =
                    getString(if (userSettings.actionModeShowCancel) R.string.hide_cancel_button else R.string.show_cancel_button)
                menu.findItem(R.id.menu_item_search_on_action_mode).title = when (userSettings.searchOnActionMode) {
                    SearchOnActionMode.DISMISS -> "SearchOnActionMode.Dismiss"
                    SearchOnActionMode.NO_DISMISS -> "SearchOnActionMode.NoDismiss"
                    SearchOnActionMode.CONCURRENT -> "SearchOnActionMode.Concurrent"
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            R.id.menu_item_search -> startSearch().let { true }

            R.id.menu_item_show_index_scroll -> {
                lifecycleScope.launch {
                    val userSettings = getUserSettings()
                    val enabled = updateUserSettings { it.copy(showIndexScroll = !userSettings.showIndexScroll) }.showIndexScroll
                    menuItem.title = getString(if (enabled) R.string.show_fast_scroller else R.string.show_index_scroll)
                    binding.iconList.seslSetFastScrollerEnabled(!enabled)
                    binding.iconIndexScroll.isVisible = enabled
                }
                true
            }

            R.id.menu_item_index_scroll_letters -> {
                lifecycleScope.launch {
                    val userSettings = getUserSettings()
                    val enabled = updateUserSettings { it.copy(indexScrollShowLetters = !userSettings.indexScrollShowLetters) }
                        .indexScrollShowLetters
                    menuItem.title = getString(if (enabled) R.string.hide_letters else R.string.show_letters)
                    binding.iconIndexScroll.setIndexBarTextMode(enabled)
                }
                true
            }

            R.id.menu_item_index_scroll_auto_hide -> {
                lifecycleScope.launch {
                    val userSettings = getUserSettings()
                    val enabled = updateUserSettings { it.copy(indexScrollAutoHide = !userSettings.indexScrollAutoHide) }
                        .indexScrollAutoHide
                    menuItem.title = getString(if (enabled) R.string.disable_auto_hide else R.string.enable_auto_hide)
                    binding.iconIndexScroll.setAutoHide(enabled)
                }
                true
            }

            R.id.menu_item_show_cancel -> {
                lifecycleScope.launch {
                    val userSettings = getUserSettings()
                    val enabled = updateUserSettings { it.copy(actionModeShowCancel = !userSettings.actionModeShowCancel) }
                        .actionModeShowCancel
                    menuItem.title = getString(if (enabled) R.string.hide_cancel_button else R.string.show_cancel_button)
                }
                true
            }

            R.id.menu_item_search_on_action_mode -> {
                lifecycleScope.launch {
                    val userSettings = getUserSettings()
                    val newMode = when (userSettings.searchOnActionMode) {
                        SearchOnActionMode.DISMISS -> SearchOnActionMode.NO_DISMISS
                        SearchOnActionMode.NO_DISMISS -> SearchOnActionMode.CONCURRENT
                        SearchOnActionMode.CONCURRENT -> SearchOnActionMode.DISMISS
                    }
                    updateUserSettings { it.copy(searchOnActionMode = newMode) }
                    menuItem.title = when (newMode) {
                        SearchOnActionMode.DISMISS -> "SearchOnActionMode.Dismiss"
                        SearchOnActionMode.NO_DISMISS -> "SearchOnActionMode.NoDismiss"
                        SearchOnActionMode.CONCURRENT -> "SearchOnActionMode.Concurrent"
                    }
                    suggestiveSnackBar(menuItem.title.toString())
                }
                true
            }

            else -> false
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)


    private fun initList() {
        binding.iconList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = IconAdapter(context, binding.iconIndexScroll).apply {
                setupOnClickListeners()
                iconAdapter = this
            }
            itemAnimator = null
            addItemDecoration(
                SemItemDecoration(
                    context,
                    dividerRule = ItemDecorRule.ALL,
                    subHeaderRule = ItemDecorRule.NONE
                ).apply {
                    setDividerInsetStart(76.dpToPx(resources))
                })
            enableCoreSeslFeatures()
            hideSoftInputOnScroll()
            configureItemSwipeAnimator()
        }
        iconAdapter.configure(
            binding.iconList,
            IconAdapter.Payload.SELECTION_MODE,
            onAllSelectorStateChanged = { allSelectorStateFlow.value = it }
        )
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
        onClickItem = { position, icon, viewHolder ->
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
            isLeftSwipeEnabled = { viewHolder -> !drawerLayout.isActionMode },
            isRightSwipeEnabled = { viewHolder -> !drawerLayout.isActionMode },
            onSwiped = { position, swipeDirection, _ ->
                val icon = iconAdapter.getItemByPosition(position)
                when (swipeDirection) {
                    START -> suggestiveSnackBar("${(icon.name)}: Right to Left")
                    END -> suggestiveSnackBar("${(icon.name)}: Left to Right")
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

        override fun onSearchModeToggle(searchView: SearchView, visible: Boolean) {
            if (visible) {
                searchView.apply {
                    seslSetOverflowMenuButtonIcon(AppCompatResources.getDrawable(requireContext(), iconsR.drawable.ic_oui_list_filter))
                    seslSetOverflowMenuButtonVisibility(VISIBLE)
                    seslSetOnOverflowMenuButtonClickListener { clearFocus(); SearchFilterBottomSheet().show(childFragmentManager, null) }
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
}
