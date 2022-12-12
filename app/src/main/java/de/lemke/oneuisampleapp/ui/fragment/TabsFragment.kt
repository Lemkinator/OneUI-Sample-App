package de.lemke.oneuisampleapp.ui.fragment

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.ui.BaseFragment
import dev.oneuiproject.oneui.dialog.GridMenuDialog
import dev.oneuiproject.oneui.utils.TabLayoutUtils

@AndroidEntryPoint
class TabsFragment : BaseFragment() {
    private lateinit var subTabs: TabLayout
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var bottomNavViewText: BottomNavigationView
    private lateinit var tabs: TabLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSubTabs(view)
        initBNV(view)
        initMainTabs(view)
    }

    override val layoutResId: Int = R.layout.fragment_tabs
    override val iconResId: Int = dev.oneuiproject.oneui.R.drawable.ic_oui_prompt_from_menu
    override val title: CharSequence = "Navigation"

    private fun initSubTabs(view: View) {
        subTabs = view.findViewById(R.id.tabs_subtab)
        subTabs.seslSetSubTabStyle()
        subTabs.tabMode = TabLayout.SESL_MODE_WEIGHT_AUTO
        subTabs.addTab(subTabs.newTab().setText("Tab 1"))
        subTabs.addTab(subTabs.newTab().setText("Tab 2"))
        subTabs.addTab(subTabs.newTab().setText("Tab 3"))
    }

    private fun initBNV(view: View) {
        bottomNavView = view.findViewById(R.id.tabs_bottomnav)
        bottomNavViewText = view.findViewById(R.id.tabs_bottomnav_text)
        bottomNavView.seslSetGroupDividerEnabled(true)
    }

    private fun initMainTabs(view: View) {
        tabs = view.findViewById(R.id.tabs_tabs)
        tabs.addTab(tabs.newTab().setText("Tab 1"))
        tabs.addTab(tabs.newTab().setText("Tab 2"))
        tabs.addTab(tabs.newTab().setText("Tab 3"))
        val gridMenuDialog = GridMenuDialog(context!!)
        gridMenuDialog.inflateMenu(R.menu.tabs_grid_menu)
        gridMenuDialog.setOnItemClickListener { true }
        TabLayoutUtils.addCustomButton(tabs, dev.oneuiproject.oneui.R.drawable.ic_oui_drawer) { gridMenuDialog.show() }
    }
}