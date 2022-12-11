package de.lemke.oneuisampleapp.ui.fragment

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.ui.BaseFragment
import dev.oneuiproject.oneui.dialog.GridMenuDialog
import dev.oneuiproject.oneui.utils.TabLayoutUtils

class TabsFragment : BaseFragment() {
    private lateinit var mSubTabs: TabLayout
    private lateinit var mBottomNavView: BottomNavigationView
    private lateinit var mBottomNavViewText: BottomNavigationView
    private lateinit var mTabs: TabLayout
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
        mSubTabs = view.findViewById(R.id.tabs_subtab)
        mSubTabs.seslSetSubTabStyle()
        mSubTabs.tabMode = TabLayout.SESL_MODE_WEIGHT_AUTO
        mSubTabs.addTab(mSubTabs.newTab().setText("Tab 1"))
        mSubTabs.addTab(mSubTabs.newTab().setText("Tab 2"))
        mSubTabs.addTab(mSubTabs.newTab().setText("Tab 3"))
    }

    private fun initBNV(view: View) {
        mBottomNavView = view.findViewById(R.id.tabs_bottomnav)
        mBottomNavViewText = view.findViewById(R.id.tabs_bottomnav_text)
        mBottomNavView.seslSetGroupDividerEnabled(true)
    }

    private fun initMainTabs(view: View) {
        mTabs = view.findViewById(R.id.tabs_tabs)
        mTabs.addTab(mTabs.newTab().setText("Tab 1"))
        mTabs.addTab(mTabs.newTab().setText("Tab 2"))
        mTabs.addTab(mTabs.newTab().setText("Tab 3"))
        val gridMenuDialog = GridMenuDialog(context!!)
        gridMenuDialog.inflateMenu(R.menu.tabs_grid_menu)
        gridMenuDialog.setOnItemClickListener { true }
        TabLayoutUtils.addCustomButton(mTabs, dev.oneuiproject.oneui.R.drawable.ic_oui_drawer) { gridMenuDialog.show() }
    }
}