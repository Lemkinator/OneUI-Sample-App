package de.lemke.oneuisample.ui

import android.content.Context
import android.graphics.ColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityAppPickerBinding
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.util.ListTypes
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.seslSetFastScrollerAdditionalPadding
import dev.oneuiproject.oneui.ktx.setEntries
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior.CLEAR_DISMISS
import dev.oneuiproject.oneui.layout.startSearchMode
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppPickerActivity : AppCompatActivity(), ViewYTranslator by AppBarAwareYTranslator() {
    private lateinit var binding: ActivityAppPickerBinding
    private val packageManagerHelper by lazy { AppPickerContext(this).packageManagerHelper }
    private var currentPicker: SeslAppPickerView? = null

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSpinner()
        binding.noEntryView.translateYWithAppBar(binding.toolbarLayout.appBarLayout, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = menuInflater.inflate(R.menu.app_picker, menu).let { true }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_app_picker_search -> binding.toolbarLayout.startSearchMode(
            onStart = { it.queryHint = "Search apps"; binding.appPickerSpinner.isEnabled = false },
            onQuery = { query, _ -> applyFilter(query); true },
            onEnd = { applyFilter(); binding.appPickerSpinner.isEnabled = true },
            onBackBehavior = CLEAR_DISMISS
        ).let { true }

        else -> false
    }

    private fun initSpinner() {
        binding.appPickerSpinner.apply {
            setEntries(ListTypes.entries.map { getString(it.description) }) { pos, _ ->
                pos?.let { type ->
                    setAppPickerType(ListTypes.entries[type])
                    lifecycleScope.launch { updateUserSettings { it.copy(appPickerType = type) } }
                }
            }
            lifecycleScope.launch { setSelection(getUserSettings().appPickerType) }
        }
    }

    private fun configureAppPicker(appPicker: SeslAppPickerView) {
        appPicker.apply {
            appListOrder = ORDER_ASCENDING
            seslSetIndexTipEnabled(true)
            seslSetFillHorizontalPaddingEnabled(true)
            seslSetFastScrollerAdditionalPadding(10.dpToPx(resources))
            setOnItemClickEventListener { _, appInfo ->
                suggestiveSnackBar("${packageManagerHelper.getAppLabel(appInfo)} clicked!")
                if (appPicker is SeslAppPickerGridView) {
                    setState(appInfo, !getState(appInfo))
                    true
                } else {
                    false
                }
            }
            setOnItemActionClickEventListener { _, appInfo ->
                suggestiveSnackBar("${packageManagerHelper.getAppLabel(appInfo)} action clicked!")
                true
            }
            setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateAllChanged(isAllSelected: Boolean) = setStateAll(isAllSelected)
                override fun onStateChanged(appInfo: AppInfo, isSelected: Boolean) {
                    val allItemsSelected = appPicker.appDataList.count { !(it as AppInfoData).selected } == 0
                    (headerFooterAdapter.getItem(0) as? AllAppsViewData)?.selectableItem?.setValueSilence(allItemsSelected)
                }
            })
        }
    }

    private fun setAppPickerType(listType: ListTypes) {
        binding.appPickerProgress.isVisible = true
        currentPicker = when (listType) {
            ListTypes.TYPE_GRID,
            ListTypes.TYPE_GRID_CHECKBOX -> binding.appPickerGrid.also {
                it.isVisible = true
                binding.appPickerList.isVisible = false
            }

            else -> binding.appPickerList.also {
                it.isVisible = true
                binding.appPickerGrid.isVisible = false
            }
        }
        configureAppPicker(currentPicker!!)
        val packages = getAppList(this, listType)
        currentPicker!!.submitList(packages)
        updateAppPickerVisibility(packages.isNotEmpty())
        binding.appPickerProgress.isVisible = false
    }

    fun getAppList(context: Context, listType: ListTypes): List<AppInfoData> {
        val actionIcon by lazy { ContextCompat.getDrawable(context, dev.oneuiproject.oneui.R.drawable.ic_oui_settings_outline) }
        return SeslAppInfoDataHelper(context, listType.builder).getPackages().onEach {
            it.subLabel = it.packageName
            if (listType == ListTypes.TYPE_LIST_ACTION_BUTTON) it.actionIcon = actionIcon
        }
    }

    private fun updateAppPickerVisibility(visible: Boolean) {
        if (visible) {
            binding.noEntryScrollView.isVisible = false
            currentPicker?.isVisible = true
        } else {
            binding.noEntryLottie.cancelAnimation()
            binding.noEntryLottie.progress = 0f
            binding.noEntryScrollView.isVisible = true
            val callback = LottieValueCallback<ColorFilter>(SimpleColorFilter(getColor(R.color.primary_color_themed)))
            binding.noEntryLottie.addValueCallback(KeyPath("**"), COLOR_FILTER, callback)
            binding.noEntryLottie.postDelayed({ binding.noEntryLottie.playAnimation() }, 400)
            currentPicker?.isVisible = false
        }
    }

    private fun applyFilter(query: String = "") {
        currentPicker!!.setSearchFilter(query) { updateAppPickerVisibility(it > 0) }
    }
}