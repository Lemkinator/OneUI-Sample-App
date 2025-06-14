package de.lemke.oneuisample.ui

import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PackageManager.PackageInfoFlags
import android.graphics.ColorFilter
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.SeslMenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.AppPickerDelegate
import dev.oneuiproject.oneui.delegates.AppPickerOp
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.setEntries
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.layout.startSearchMode
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityAppPickerBinding
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppPickerActivity : AppCompatActivity(), ViewYTranslator by AppBarAwareYTranslator(), AppPickerOp by AppPickerDelegate() {
    private lateinit var binding: ActivityAppPickerBinding
    private var showSystemApps = false
    private var search = ""

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    data class App(val packageName: String, val isSystemApp: Boolean)

    private val apps: List<App> by lazy {
        packageManager.getInstalledPackagesCompat(GET_META_DATA).map {
            App(packageName = it.packageName, isSystemApp = (it.applicationInfo?.flags?.and(FLAG_SYSTEM) ?: 1) != 0)
        }
    }

    private val packageNames get() = apps.filter { showSystemApps || !it.isSystemApp }.map { it.packageName }

    fun PackageManager.getInstalledPackagesCompat(flags: Int = 0): List<PackageInfo> =
        if (SDK_INT >= TIRAMISU) getInstalledPackages(PackageInfoFlags.of(flags.toLong()))
        else getInstalledPackages(0)

    private fun refreshApps() {
        binding.appPickerProgress.isVisible = true
        refreshAppList()
        binding.appPickerProgress.isVisible = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appPicker.apply {
            itemAnimator = null
            seslSetSmoothScrollEnabled(true)
            configure(
                lifecycleOwner = this@AppPickerActivity,
                onGetCurrentList = { ArrayList(packageNames) },
                onItemClicked = { _, _, appLabel -> suggestiveSnackBar("$appLabel clicked!") },
                onItemCheckChanged = { _, _, appLabel, isChecked -> },
                onSelectAllChanged = { _, isChecked -> },
                onItemActionClicked = { _, _, appLabel -> suggestiveSnackBar("$appLabel action button clicked!") }
            )
        }
        initSpinner()
        lifecycleScope.launch {
            showSystemApps = getUserSettings().showSystemApps
            refreshApps()
        }
        binding.noEntryView.translateYWithAppBar(binding.toolbarLayout.appBarLayout, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_picker, menu)
        val systemAppsItem = menu.findItem(R.id.menu_item_app_picker_system)
        (systemAppsItem as SeslMenuItem).badgeText = getString(dev.oneuiproject.oneui.design.R.string.oui_des_new_badge_text)
        lifecycleScope.launch {
            showSystemApps = getUserSettings().showSystemApps
            systemAppsItem.title = getString(if (showSystemApps) R.string.hide_system_apps else R.string.show_system_apps)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_app_picker_search -> binding.toolbarLayout.startSearchMode(
            onStart = { it.queryHint = "Search app"; },
            onQuery = { query, _ -> search = query; applyFilter(query); true },
            onEnd = { applyFilter() },
            onBackBehavior = ToolbarLayout.SearchModeOnBackBehavior.CLEAR_DISMISS
        ).let { true }

        R.id.menu_item_app_picker_system -> {
            (item as SeslMenuItem).badgeText = null
            showSystemApps = !showSystemApps
            lifecycleScope.launch { updateUserSettings { it.copy(showSystemApps = showSystemApps) } }
            item.title = getString(if (showSystemApps) R.string.hide_system_apps else R.string.show_system_apps)
            refreshApps()
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
                    if (binding.toolbarLayout.isSearchMode) applyFilter(search)
                    lifecycleScope.launch { updateUserSettings { it.copy(appPickerType = position) } }
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