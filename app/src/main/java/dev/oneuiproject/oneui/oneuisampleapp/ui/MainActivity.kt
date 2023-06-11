package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.app.SearchManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.dialog.GridMenuDialog
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.ActivityMainBinding
import dev.oneuiproject.oneui.oneuisampleapp.domain.AppStart
import dev.oneuiproject.oneui.oneuisampleapp.domain.CheckAppStartUseCase
import dev.oneuiproject.oneui.oneuisampleapp.domain.GetUserSettingsUseCase
import dev.oneuiproject.oneui.oneuisampleapp.domain.UpdateUserSettingsUseCase
import dev.oneuiproject.oneui.oneuisampleapp.ui.dialog.SearchFilterDialog
import dev.oneuiproject.oneui.oneuisampleapp.ui.fragments.MainActivitySearchFragment
import dev.oneuiproject.oneui.oneuisampleapp.ui.fragments.MainActivityTabDesign
import dev.oneuiproject.oneui.oneuisampleapp.ui.fragments.MainActivityTabIcons
import dev.oneuiproject.oneui.utils.TabLayoutUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var onBackInvokedCallback: OnBackInvokedCallback
    private val fragmentsInstance: List<Fragment> =
        listOf(MainActivityTabDesign(), MainActivityTabIcons(), MainActivitySearchFragment())
    private val searchFragmentIndex = 2
    private var selectedPosition = -1
    private var isSearchFragmentVisible = false
    private var isSearchUserInputEnabled = false
    private var time: Long = 0
    private var isUIReady = false

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var checkAppStart: CheckAppStartUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        /*  Note: https://stackoverflow.com/a/69831106/18332741
        On Android 12 just running the app via android studio doesn't show the full splash screen.
        You have to kill it and open the app from the launcher.
        */
        val splashScreen = installSplashScreen()
        time = System.currentTimeMillis()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        splashScreen.setKeepOnScreenCondition { !isUIReady }
        /*
        there is a bug in the new splash screen api, when using the onExitAnimationListener -> splash icon flickers
        therefore setting a manual delay in openMain()
        splashScreen.setOnExitAnimationListener { splash ->
            val splashAnimator: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                splash.view,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f)
            )
            splashAnimator.interpolator = AccelerateDecelerateInterpolator()
            splashAnimator.duration = 400L
            splashAnimator.doOnEnd { splash.remove() }
            val contentAnimator: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                binding.root,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f, 1f)
            )
            contentAnimator.interpolator = AccelerateDecelerateInterpolator()
            contentAnimator.duration = 400L

            val remainingDuration = splash.iconAnimationDurationMillis - (System.currentTimeMillis() - splash.iconAnimationStartMillis)
                .coerceAtLeast(0L)
            lifecycleScope.launch {
                delay(remainingDuration)
                splashAnimator.start()
                contentAnimator.start()
            }
        }*/

        lifecycleScope.launch {
            when (checkAppStart()) {
                AppStart.FIRST_TIME -> openOOBE()
                AppStart.NORMAL -> checkTOS()
                AppStart.FIRST_TIME_VERSION -> checkTOS()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            delay(500) //delay, so closing the drawer is not visible for the user
            binding.drawerLayoutMain.setDrawerOpen(false, false)
        }
    }

    private suspend fun openOOBE() {
        //manually waiting for the animation to finish :/
        delay(500 - (System.currentTimeMillis() - time).coerceAtLeast(0L))
        startActivity(Intent(applicationContext, OOBEActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private suspend fun checkTOS() {
        if (!getUserSettings().tosAccepted) openOOBE()
        else openMain()
    }

    private fun openMain() {
        initOnBackPressed()
        initDrawer()
        initTabLayout()
        initFragments()
        lifecycleScope.launch {
            //manually waiting for the animation to finish :/
            delay(500 - (System.currentTimeMillis() - time).coerceAtLeast(0L))
            isUIReady = true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.action == Intent.ACTION_SEARCH) binding.drawerLayoutMain.searchView.setQuery(
            intent.getStringExtra(SearchManager.QUERY),
            true
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                binding.drawerLayoutMain.showSearchMode()
                setSearchFragment()
                return true
            }

            R.id.menu_custom_about_app -> {
                startActivity(Intent(this, CustomAboutActivity::class.java))
                return true
            }
        }
        return false
    }

    private fun initFragments() {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        for (fragment in fragmentsInstance) transaction.add(R.id.fragment_container, fragment)
        transaction.commitAllowingStateLoss()
        supportFragmentManager.executePendingTransactions()
        setFragment(0)
    }

    fun setFragment(position: Int, tab: TabLayout.Tab? = null) {
        onBackPressedCallback.isEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCallback)
        }
        val newFragment: Fragment = fragmentsInstance[position]
        if (selectedPosition != position || isSearchFragmentVisible) {
            selectedPosition = position
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            for (fragment in supportFragmentManager.fragments) transaction.hide(fragment)
            transaction.show(newFragment).commitAllowingStateLoss()
            supportFragmentManager.executePendingTransactions()
            val newTab = tab ?: binding.mainTabs.getTabAt(position)
            if (newTab?.isSelected == false) newTab.select()
        }
        isSearchFragmentVisible = false
    }

    fun setSearchFragment() {
        //disable back pressing on search fragment by setting a custom to quit search -> (otherwise would exit on backpressed)
        onBackPressedCallback.isEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(PRIORITY_DEFAULT, onBackInvokedCallback)
        }
        val newFragment: Fragment = fragmentsInstance[searchFragmentIndex]
        if (!isSearchFragmentVisible) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            for (fragment in supportFragmentManager.fragments) transaction.hide(fragment)
            transaction.show(newFragment).commitAllowingStateLoss()
            supportFragmentManager.executePendingTransactions()
        }
        (newFragment as OnDataChangedListener).onDataChanged()
        isSearchFragmentVisible = true
    }


    private fun initDrawer() {
        val oobeOption = findViewById<LinearLayout>(R.id.draweritem_oobe)
        val aboutAppOption = findViewById<LinearLayout>(R.id.draweritem_about_app)
        val customAboutAppOption = findViewById<LinearLayout>(R.id.draweritem_custom_about_app)
        val settingsOption = findViewById<LinearLayout>(R.id.draweritem_settings)

        oobeOption.setOnClickListener { startActivity(Intent(this@MainActivity, OOBEActivity::class.java)) }
        aboutAppOption.setOnClickListener { startActivity(Intent(this@MainActivity, AboutActivity::class.java)) }
        customAboutAppOption.setOnClickListener { startActivity(Intent(this@MainActivity, CustomAboutActivity::class.java)) }
        settingsOption.setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        binding.drawerLayoutMain.setDrawerButtonIcon(getDrawable(dev.oneuiproject.oneui.R.drawable.ic_oui_info_outline))
        binding.drawerLayoutMain.setDrawerButtonOnClickListener {
            startActivity(Intent().setClass(this@MainActivity, AboutActivity::class.java))
        }
        binding.drawerLayoutMain.setDrawerButtonTooltip(getText(R.string.about_app))
        binding.drawerLayoutMain.setSearchModeListener(SearchModeListener())
        binding.drawerLayoutMain.searchView.setSearchableInfo(
            (getSystemService(SEARCH_SERVICE) as SearchManager).getSearchableInfo(componentName)
        )
        binding.drawerLayoutMain.searchView.seslSetOverflowMenuButtonIcon(getDrawable(dev.oneuiproject.oneui.R.drawable.ic_oui_list_filter))
        binding.drawerLayoutMain.searchView.seslSetOverflowMenuButtonVisibility(View.VISIBLE)
        binding.drawerLayoutMain.searchView.seslSetOnOverflowMenuButtonClickListener {
            SearchFilterDialog { setSearchFragment() }.show(supportFragmentManager, "")
        }
    }

    inner class SearchModeListener : ToolbarLayout.SearchModeListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            if (!isSearchUserInputEnabled) return false
            lifecycleScope.launch {
                updateUserSettings { it.copy(search = query ?: "") }
                setSearchFragment()
            }
            return true
        }

        override fun onQueryTextChange(query: String?): Boolean {
            if (!isSearchUserInputEnabled) return false
            lifecycleScope.launch {
                updateUserSettings { it.copy(search = query ?: "") }
                setSearchFragment()
            }
            return true
        }

        override fun onSearchModeToggle(searchView: SearchView, visible: Boolean) {
            if (visible) {
                isSearchUserInputEnabled = true
                lifecycleScope.launch {
                    val search = getUserSettings().search
                    searchView.setQuery(search, false)
                    val autoCompleteTextView = searchView.seslGetAutoCompleteView()
                    autoCompleteTextView.setText(search)
                    autoCompleteTextView.setSelection(autoCompleteTextView.text.length)
                }
            } else {
                isSearchUserInputEnabled = false
                setFragment(selectedPosition)
            }
        }
    }

    private fun initTabLayout() {
        binding.mainTabs.tabMode = TabLayout.SESL_MODE_FIXED_AUTO
        binding.mainTabs.addTab(binding.mainTabs.newTab().setText(getString(R.string.design)))
        binding.mainTabs.addTab(binding.mainTabs.newTab().setText(getString(R.string.icons)))
        val gridMenuDialog = GridMenuDialog(this)
        gridMenuDialog.inflateMenu(R.menu.tabs_grid_menu)
        gridMenuDialog.setOnItemClickListener {
            when (it.itemId) {
                R.id.grid_menu_seek_bar -> {
                    startActivity(Intent(this@MainActivity, SeekBarActivity::class.java))
                    return@setOnItemClickListener true
                }

                R.id.grid_menu_pickers -> {
                    startActivity(Intent(this@MainActivity, PickersActivity::class.java))
                    return@setOnItemClickListener true
                }

                R.id.grid_menu_index_scroll -> {
                    startActivity(Intent(this@MainActivity, IndexScrollActivity::class.java))
                    return@setOnItemClickListener true
                }

                R.id.grid_menu_app_picker_view -> {
                    startActivity(Intent(this@MainActivity, AppPickerActivity::class.java))
                    return@setOnItemClickListener true
                }

                else -> return@setOnItemClickListener false
            }
        }
        TabLayoutUtils.addCustomButton(binding.mainTabs, dev.oneuiproject.oneui.R.drawable.ic_oui_drawer) { gridMenuDialog.show() }
        binding.mainTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                setFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                try {
                    when (tab.text) {
                        getString(R.string.design) -> {
                            val subTabs: TabLayout = findViewById(R.id.fragment_design_sub_tabs)
                            val newTabIndex = subTabs.selectedTabPosition + 1
                            if (newTabIndex < subTabs.tabCount) subTabs.getTabAt(newTabIndex)?.select()
                            else subTabs.getTabAt(0)?.select()
                        }

                        getString(R.string.icons) -> {
                            val iconsRecyclerView: RecyclerView = findViewById(R.id.icons_recycler_view)
                            if (iconsRecyclerView.canScrollVertically(-1)) iconsRecyclerView.smoothScrollToPosition(0)
                            else binding.drawerLayoutMain.setExpanded(!binding.drawerLayoutMain.isExpanded, true)
                        }
                    }
                } catch (e: Exception) { //no required functionality -> ignore errors
                    Log.e("MainActivity", "Error while reselecting tab", e)
                }
            }
        })
    }

    private fun initOnBackPressed() {
        //set custom callback to prevent app from exiting on back press when in search mode
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                checkBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedCallback = OnBackInvokedCallback { checkBackPressed() }
        }
    }

    private fun checkBackPressed() {
        if (binding.drawerLayoutMain.isSearchMode) {
            isSearchUserInputEnabled = false
            binding.drawerLayoutMain.dismissSearchMode()
        } else finishAffinity() //should not get here bc callbacks are only enabled in search mode
    }
}

interface OnDataChangedListener {
    fun onDataChanged()
}