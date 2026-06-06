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
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.userSettings
import de.lemke.oneuisample.data.withListener
import de.lemke.oneuisample.databinding.DialogSettingsBinding
import de.lemke.oneuisample.databinding.FragmentTabIconsBinding
import de.lemke.oneuisample.domain.ObserveIconListUseCase
import de.lemke.oneuisample.domain.autoCleared
import de.lemke.oneuisample.domain.getSearchListener
import de.lemke.oneuisample.domain.launchAndRepeatWithViewLifecycle
import de.lemke.oneuisample.domain.suggestiveSnackBar
import de.lemke.oneuisample.ui.util.IconAdapter
import de.lemke.oneuisample.ui.util.IconAdapter.Icon
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
class TabIcons : AbsBaseFragment(R.layout.fragment_tab_icons), ViewYTranslator by AppBarAwareYTranslator() {
    private val binding by autoCleared { FragmentTabIconsBinding.bind(requireView()) }
    private lateinit var drawerLayout: DrawerLayout
    private val allSelectorStateFlow: MutableStateFlow<AllSelectorState> = MutableStateFlow(AllSelectorState())
    private val iconAdapter: IconAdapter by lazy {
        IconAdapter(
            requireContext(),
            onAllSelectorStateChanged = { allSelectorStateFlow.value = it },
            onBlockActionMode = ::launchActionMode,
        )
    }

    @Inject
    lateinit var observeIconList: ObserveIconListUseCase

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
        binding.noEntryView.translateYWithAppBar(requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout).appBarLayout, this)
    }

    private fun setupMenuProvider() =
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater,
                ) = menuInflater.inflate(R.menu.icon_tab_menu, menu)

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.menu_item_search -> startSearch().let { true }
                        R.id.menu_item_settings -> showSettingsDialog().let { true }
                        else -> false
                    }
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
        binding.iconList.seslSetFastScrollerEnabled(!userSettings.showIndexScroll)
        binding.iconIndexScroll.isVisible = userSettings.showIndexScroll
        binding.iconIndexScroll.setIndexBarTextMode(userSettings.indexScrollShowLetters)
        binding.iconIndexScroll.setAutoHide(userSettings.indexScrollAutoHide)
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
        onClickItem = { position, icon, _ ->
            if (isActionMode) {
                toggleItem(icon.id, position)
            } else {
                suggestiveSnackBar(icon.beautifiedName, actionText = getString(R.string.ok))
            }
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
            },
        )
    }

    private fun startSearch() = drawerLayout.startSearchMode(searchModeListener, DISMISS)

    val searchModeListener =
        getSearchListener {
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

    private fun launchActionMode(initialSelected: Set<Long>? = null) {
        iconAdapter.toggleActionMode(true, initialSelected)
        drawerLayout.startActionMode(
            onInflateMenu = { menu, menuInflater -> menuInflater.inflate(R.menu.select, menu) },
            onEnd = { iconAdapter.toggleActionMode(false) },
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

                    else -> {
                        false
                    }
                }
            },
            onSelectAll = { isChecked: Boolean -> iconAdapter.onToggleSelectAll(isChecked) },
            allSelectorStateFlow = allSelectorStateFlow,
            searchOnActionMode = userSettings.searchOnActionMode.withListener(searchModeListener),
            showCancel = userSettings.actionModeShowCancel,
        )
    }

    private fun showSettingsDialog() {
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
                    indexScrollShowLetters.isEnabled = isChecked
                    indexScrollAutoHide.isEnabled = isChecked
                }
            }
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.settings))
            setView(dialogBinding.root)
            setNegativeButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_cancel), null)
            setPositiveButton(getString(dev.oneuiproject.oneui.design.R.string.oui_des_common_apply)) { _, _ ->
                userSettings.update {
                    copy(
                        actionModeShowCancel = dialogBinding.actionModeShowCancel.isChecked,
                        showIndexScroll = dialogBinding.showIndexScroll.isChecked,
                        indexScrollShowLetters = dialogBinding.indexScrollShowLetters.isChecked,
                        indexScrollAutoHide = dialogBinding.indexScrollAutoHide.isChecked,
                        searchOnActionMode =
                            when (dialogBinding.amsOptions.checkedRadioButtonId) {
                                R.id.amsDismiss -> ToolbarLayout.SearchOnActionMode.Dismiss
                                R.id.amsNoDismiss -> ToolbarLayout.SearchOnActionMode.NoDismiss
                                else -> ToolbarLayout.SearchOnActionMode.Concurrent(null)
                            },
                    )
                }
                binding.iconList.seslSetFastScrollerEnabled(!userSettings.showIndexScroll)
                binding.iconIndexScroll.isVisible = userSettings.showIndexScroll
                binding.iconIndexScroll.setIndexBarTextMode(userSettings.indexScrollShowLetters)
                binding.iconIndexScroll.setAutoHide(userSettings.indexScrollAutoHide)
            }
            show()
        }
    }
}
