package de.lemke.oneuisampleapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.databinding.ActivityMainBinding
import de.lemke.oneuisampleapp.ui.drawer.DrawerListAdapter
import de.lemke.oneuisampleapp.ui.fragment.*

class MainActivity : AppCompatActivity(), DrawerListAdapter.DrawerListener {
    private lateinit var binding: ActivityMainBinding
    private val fragments: MutableList<Fragment?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFragmentList()
        initDrawer()
        initFragments()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_about_app) {
            startActivity(Intent(this, CustomAboutActivity::class.java))
            return true
        }
        return false
    }

    private fun initDrawer() {
        binding.drawerLayout.setDrawerButtonIcon(getDrawable(dev.oneuiproject.oneui.design.R.drawable.oui_ic_ab_app_info))
        binding.drawerLayout.setDrawerButtonTooltip("About page")
        binding.drawerLayout.setDrawerButtonOnClickListener {
            startActivity(Intent(this@MainActivity, AboutActivity::class.java))
        }
        binding.drawerListView.layoutManager = LinearLayoutManager(this)
        binding.drawerListView.adapter = DrawerListAdapter(this, fragments, this)
        binding.drawerListView.itemAnimator = null
        binding.drawerListView.setHasFixedSize(true)
        binding.drawerListView.seslSetLastRoundedCorner(false)
    }

    private fun initFragmentList() {
        fragments.add(WidgetsFragment())
        fragments.add(ProgressBarFragment())
        fragments.add(SeekBarFragment())
        fragments.add(SwipeRefreshFragment())
        fragments.add(PreferencesFragment())
        fragments.add(null)
        fragments.add(TabsFragment())
        fragments.add(null)
        fragments.add(AppPickerFragment())
        fragments.add(IndexScrollFragment())
        fragments.add(PickersFragment())
        fragments.add(null)
        fragments.add(IconsFragment())
    }

    private fun initFragments() {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in fragments) {
            if (fragment != null) transaction.add(R.id.main_content, fragment)
        }
        transaction.commit()
        supportFragmentManager.executePendingTransactions()
        onDrawerItemSelected(0)
    }

    override fun onDrawerItemSelected(position: Int): Boolean {
        val newFragment = fragments[position]
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in supportFragmentManager.fragments) {
            transaction.hide(fragment!!)
        }
        transaction.show(newFragment!!).commit()
        if (newFragment is FragmentInfo) {
            if (!(newFragment as FragmentInfo).isAppBarEnabled) {
                binding.drawerLayout.setExpanded(false, false)
                binding.drawerLayout.isExpandable = false
            } else {
                binding.drawerLayout.isExpandable = true
                binding.drawerLayout.setExpanded(false, false)
            }
            binding.drawerLayout.setTitle(getString(R.string.app_name), (newFragment as FragmentInfo).title)
            binding.drawerLayout.setExpandedSubtitle((newFragment as FragmentInfo).title)
        }
        binding.drawerLayout.setDrawerOpen(false, true)
        return true
    }
}