package de.lemke.oneuisampleapp.ui.fragment

import de.lemke.oneuisampleapp.ui.BaseFragment
import androidx.apppickerview.widget.AppPickerView.OnBindListener
import android.widget.AdapterView
import androidx.apppickerview.widget.AppPickerView
import androidx.appcompat.widget.SeslProgressBar
import android.os.Bundle
import de.lemke.oneuisampleapp.R
import android.view.MenuInflater
import androidx.appcompat.view.menu.SeslMenuItem
import androidx.appcompat.widget.AppCompatSpinner
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisampleapp.domain.GetUserSettingsUseCase
import de.lemke.oneuisampleapp.domain.UpdateUserSettingsUseCase
import dev.oneuiproject.oneui.widget.Toast
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AppPickerFragment : BaseFragment(), OnBindListener, AdapterView.OnItemSelectedListener {
    private var listInitialized = false
    private var listType = AppPickerView.TYPE_LIST
    private var showSystemApps = false
    private val items: MutableList<Boolean> = ArrayList()
    private var isAllAppsSelected = false
    private var checkedPosition = 0
    private lateinit var appPickerView: AppPickerView
    private lateinit var progress: SeslProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress = view.findViewById(R.id.apppicker_progress)
        appPickerView = view.findViewById(R.id.apppicker_list)
        appPickerView.itemAnimator = null
        appPickerView.seslSetSmoothScrollEnabled(true)
        lifecycleScope.launch {
            showSystemApps = getUserSettings().showSystemApps
            initSpinner(view)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && !listInitialized) {
            fillListView()
            listInitialized = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val systemAppsItem = menu.findItem(R.id.menu_apppicker_system)
        systemAppsItem.isVisible = true
        (systemAppsItem as SeslMenuItem).badgeText = getString(dev.oneuiproject.oneui.design.R.string.oui_new_badge_text)
        lifecycleScope.launch {
            showSystemApps = getUserSettings().showSystemApps
            systemAppsItem.title = getString(if (showSystemApps) R.string.hide_system_apps else R.string.show_system_apps)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_apppicker_system) {
            (item as SeslMenuItem).badgeText = null
            showSystemApps = !showSystemApps
            lifecycleScope.launch { updateUserSettings { it.copy(showSystemApps = showSystemApps) } }
            item.title = getString(if (showSystemApps) R.string.hide_system_apps else R.string.show_system_apps)
            refreshListView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override val layoutResId: Int = R.layout.fragment_apppicker
    override val iconResId: Int = dev.oneuiproject.oneui.R.drawable.ic_oui_all_apps
    override val title: CharSequence = "AppPickerView"

    private fun initSpinner(view: View) {
        val spinner = view.findViewById<AppCompatSpinner>(R.id.apppicker_spinner)
        val categories: MutableList<String> = ArrayList()
        categories.add("List")
        categories.add("List, Action Button")
        categories.add("List, CheckBox")
        categories.add("List, CheckBox, All apps")
        categories.add("List, RadioButton")
        categories.add("List, Switch")
        categories.add("List, Switch, All apps")
        categories.add("Grid")
        categories.add("Grid, CheckBox")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        listType = position
        fillListView()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private fun fillListView() {
        isAllAppsSelected = false
        showProgressCircle(true)
        object : Thread() {
            override fun run() {
                if (!listInitialized) {
                    try {
                        sleep(1000)
                    } catch (ignored: InterruptedException) {
                    }
                }
                requireActivity().runOnUiThread {
                    val installedAppSet = ArrayList(installedPackageNameUnmodifiableSet)
                    if (appPickerView.itemDecorationCount > 0) {
                        for (i in 0 until appPickerView.itemDecorationCount) {
                            appPickerView.removeItemDecorationAt(i)
                        }
                    }
                    appPickerView.setAppPickerView(listType, installedAppSet, AppPickerView.ORDER_ASCENDING_IGNORE_CASE)
                    appPickerView.setOnBindListener(this@AppPickerFragment)
                    items.clear()
                    if (listType == AppPickerView.TYPE_LIST_CHECKBOX_WITH_ALL_APPS
                        || listType == AppPickerView.TYPE_LIST_SWITCH_WITH_ALL_APPS
                    ) {
                        items.add(java.lang.Boolean.FALSE)
                    }
                    for (app in installedAppSet) {
                        items.add(java.lang.Boolean.FALSE)
                    }
                    showProgressCircle(false)
                }
            }
        }.start()
    }

    private fun refreshListView() {
        showProgressCircle(true)
        object : Thread() {
            override fun run() {
                requireActivity().runOnUiThread {
                    val installedAppSet = ArrayList(installedPackageNameUnmodifiableSet)
                    appPickerView.resetPackages(installedAppSet)
                    items.clear()
                    if (listType == AppPickerView.TYPE_LIST_CHECKBOX_WITH_ALL_APPS
                        || listType == AppPickerView.TYPE_LIST_SWITCH_WITH_ALL_APPS
                    ) {
                        items.add(java.lang.Boolean.FALSE)
                    }
                    for (app in installedAppSet) {
                        items.add(java.lang.Boolean.FALSE)
                    }
                    showProgressCircle(false)
                }
            }
        }.start()
    }

    override fun onBindViewHolder(
        holder: AppPickerView.ViewHolder,
        position: Int, packageName: String
    ) {
        when (listType) {
            AppPickerView.TYPE_LIST -> holder.item.setOnClickListener { }
            AppPickerView.TYPE_LIST_ACTION_BUTTON ->
                holder.actionButton.setOnClickListener { Toast.makeText(context, "onClick", Toast.LENGTH_SHORT).show() }
            AppPickerView.TYPE_LIST_CHECKBOX -> {
                val checkBox = holder.checkBox
                checkBox.isChecked = items[position]
                checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean -> items[position] = isChecked }
            }
            AppPickerView.TYPE_LIST_CHECKBOX_WITH_ALL_APPS -> {
                val checkBox = holder.checkBox
                if (position == 0) {
                    holder.appLabel.text = getString(R.string.all_apps)
                    checkBox.isChecked = isAllAppsSelected
                    checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                        if (isAllAppsSelected != isChecked) {
                            isAllAppsSelected = isChecked
                            var i = 0
                            while (i < items.size) {
                                items[i] = isAllAppsSelected
                                i++
                            }
                            appPickerView.refreshUI()
                        }
                    }
                } else {
                    checkBox.isChecked = items[position]
                    checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                        items[position] = isChecked
                        checkAllAppsToggle()
                    }
                }
            }
            AppPickerView.TYPE_LIST_RADIOBUTTON -> {
                val radioButton = holder.radioButton
                radioButton.isChecked = items[position]
                holder.radioButton.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    if (isChecked) {
                        if (checkedPosition != position) {
                            items[checkedPosition] = false
                            appPickerView.refreshUI(checkedPosition)
                        }
                        items[position] = true
                        checkedPosition = position
                    }
                }
            }
            AppPickerView.TYPE_LIST_SWITCH -> {
                val switchWidget = holder.switch
                switchWidget.isChecked = items[position]
                switchWidget.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean -> items[position] = isChecked }
            }
            AppPickerView.TYPE_LIST_SWITCH_WITH_ALL_APPS -> {
                val switchWidget = holder.switch
                if (position == 0) {
                    holder.appLabel.text = getString(R.string.all_apps)
                    switchWidget.isChecked = isAllAppsSelected
                    switchWidget.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                        if (isAllAppsSelected != isChecked) {
                            isAllAppsSelected = isChecked
                            var i = 0
                            while (i < items.size) {
                                items[i] = isAllAppsSelected
                                i++
                            }
                            appPickerView.refreshUI()
                        }
                    }
                } else {
                    switchWidget.isChecked = items[position]
                    switchWidget.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                        items[position] = isChecked
                        checkAllAppsToggle()
                    }
                }
            }
            AppPickerView.TYPE_GRID -> holder.item.setOnClickListener { }
            AppPickerView.TYPE_GRID_CHECKBOX -> {
                val checkBox = holder.checkBox
                checkBox.isChecked = items[position]
                checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean -> items[position] = isChecked }
                holder.item.setOnClickListener { checkBox.isChecked = !checkBox.isChecked }
            }
        }
    }

    private fun checkAllAppsToggle() {
        isAllAppsSelected = true
        for (selected in items) {
            if (!selected) {
                isAllAppsSelected = false
                break
            }
        }
        appPickerView.refreshUI(0)
    }

    private fun showProgressCircle(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
        appPickerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private val installedPackageNameUnmodifiableSet: Set<String>
        get() {
            val set = HashSet<String>()
            for (appInfo in installedAppList) {
                set.add(appInfo.packageName)
            }
            return Collections.unmodifiableSet(set)
        }

    private val installedAppList: List<ApplicationInfo>
        get() {
            val list = ArrayList<ApplicationInfo>()
            val apps = context!!.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in apps) {
                if (appInfo.flags and (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) > 0 && !showSystemApps) {
                    continue
                }
                list.add(appInfo)
            }
            return list
        }
}