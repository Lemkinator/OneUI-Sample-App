package de.lemke.oneuisample.ui

import android.graphics.ColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.SeslMenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityAppPickerBinding
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.domain.ObserveAppsUseCase
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.AppPickerDelegate
import dev.oneuiproject.oneui.delegates.AppPickerOp
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.setEntries
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior.CLEAR_DISMISS
import dev.oneuiproject.oneui.layout.startSearchMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.oneuiproject.oneui.design.R as designR

@AndroidEntryPoint
class AppPickerActivity : AppCompatActivity(), ViewYTranslator by AppBarAwareYTranslator(), AppPickerOp by AppPickerDelegate() {
    private lateinit var binding: ActivityAppPickerBinding
    private var apps: ArrayList<String> = ArrayList()
    private var search = ""

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var observeApps: ObserveAppsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appPicker.apply {
            itemAnimator = null
            seslSetSmoothScrollEnabled(true)
            configure(
                lifecycleOwner = this@AppPickerActivity,
                onGetCurrentList = { apps },
                onItemClicked = { _, _, appLabel -> suggestiveSnackBar("$appLabel clicked!") },
                onItemCheckChanged = { _, _, appLabel, isChecked -> },
                onSelectAllChanged = { _, isChecked -> },
                onItemActionClicked = { _, _, appLabel -> suggestiveSnackBar("$appLabel action button clicked!") }
            )
        }
        initSpinner()
        lifecycleScope.launch {
            observeApps().flowWithLifecycle(lifecycle, CREATED).collectLatest {
                apps = ArrayList(it)
                refreshAppList()
                binding.appPickerProgress.isVisible = false
            }
        }
        binding.noEntryView.translateYWithAppBar(binding.toolbarLayout.appBarLayout, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_picker, menu)
        val systemAppsItem = menu.findItem(R.id.menu_item_app_picker_system)
        (systemAppsItem as SeslMenuItem).badgeText = getString(designR.string.oui_des_new_badge_text)
        lifecycleScope.launch {
            systemAppsItem.title = getString(if (getUserSettings().showSystemApps) R.string.hide_system_apps else R.string.show_system_apps)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_app_picker_search -> binding.toolbarLayout.startSearchMode(
            onStart = { it.queryHint = "Search apps"; },
            onQuery = { query, _ -> search = query; applyFilter(query); true },
            onEnd = { applyFilter() },
            onBackBehavior = CLEAR_DISMISS
        ).let { true }

        R.id.menu_item_app_picker_system -> {
            (item as SeslMenuItem).badgeText = null
            lifecycleScope.launch {
                item.title = getString(
                    if (updateUserSettings { it.copy(showSystemApps = !it.showSystemApps) }.showSystemApps) R.string.hide_system_apps
                    else R.string.show_system_apps
                )
            }
            true
        }

        else -> false
    }

    private fun initSpinner() {
        binding.appPickerSpinner.apply {
            val categories = listOf(
                "List", "List, Action Button", "List, CheckBox", "List, CheckBox, All apps", "List, RadioButton",
                "List, Switch", "List, Switch, All apps", "Grid", "Grid, CheckBox"
            )
            setEntries(categories) { position, _ ->
                position?.let {
                    setListType(position)
                    lifecycleScope.launch {
                        if (binding.toolbarLayout.isSearchMode) {
                            delay(100); applyFilter(search)
                        }
                        updateUserSettings { it.copy(appPickerType = position) }
                    }
                }
            }
            lifecycleScope.launch { setSelection(getUserSettings().appPickerType) }
        }
    }

    private fun applyFilter(query: String = "") {
        binding.appPicker.setSearchFilter(query) { itemCount ->
            if (itemCount <= 0) {
                binding.appPicker.isVisible = false
                binding.noEntryLottie.cancelAnimation()
                binding.noEntryLottie.progress = 0f
                binding.noEntryScrollView.isVisible = true
                val callback = LottieValueCallback<ColorFilter>(SimpleColorFilter(getColor(R.color.primary_color_themed)))
                binding.noEntryLottie.addValueCallback(KeyPath("**"), COLOR_FILTER, callback)
                binding.noEntryLottie.postDelayed({ binding.noEntryLottie.playAnimation() }, 400)
            } else {
                binding.noEntryScrollView.isVisible = false
                binding.appPicker.isVisible = true
            }
        }
    }
}