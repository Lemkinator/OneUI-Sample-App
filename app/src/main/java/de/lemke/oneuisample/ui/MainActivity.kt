package de.lemke.oneuisample.ui

import android.R.anim.fade_in
import android.R.anim.fade_out
import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.model.ButtonModel
import com.google.android.material.appbar.model.SuggestAppBarModel
import com.google.android.material.appbar.model.view.SuggestAppBarView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityMainBinding
import de.lemke.oneuisample.domain.AppStart
import de.lemke.oneuisample.domain.CheckAppStartUseCase
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.ui.fragments.FragmentBottomSheet
import de.lemke.oneuisample.ui.fragments.TabDesign
import de.lemke.oneuisample.ui.fragments.TabIcons
import de.lemke.oneuisample.ui.fragments.TabPicker
import de.lemke.oneuisample.ui.util.onNavigationSingleClick
import de.lemke.oneuisample.ui.util.setupHeaderAndNavRail
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding
    private var selectedPosition = -1
    private var time: Long = 0
    private var isUIReady = false

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var checkAppStart: CheckAppStartUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        time = System.currentTimeMillis()
        super.onCreate(savedInstanceState)
        if (SDK_INT >= 34) overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, fade_in, fade_out)
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

    private suspend fun openOOBE() {
        //manually waiting for the animation to finish :/
        delay(700 - (System.currentTimeMillis() - time).coerceAtLeast(0L))
        startActivity(Intent(applicationContext, OOBEActivity::class.java))
        @Suppress("DEPRECATION") if (SDK_INT < 34) overridePendingTransition(fade_in, fade_out)
        finishAfterTransition()
    }

    private suspend fun checkTOS() {
        if (!getUserSettings().tosAccepted) openOOBE()
        else openMain()
    }

    private fun openMain() {
        initDrawer()
        initTabLayout()
        initFragments()
        lifecycleScope.launch {
            //manually waiting for the animation to finish :/
            delay(700 - (System.currentTimeMillis() - time).coerceAtLeast(0L))
            isUIReady = true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == ACTION_SEARCH) binding.drawerLayout.setSearchQueryFromIntent(intent)
    }

    private fun initFragments() {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        listOf(TabDesign(), TabPicker(), TabIcons()).forEach { transaction.add(R.id.fragmentContainer, it) }
        transaction.commitNowAllowingStateLoss()
        setFragment(0)
    }

    fun setFragment(position: Int) {
        if (selectedPosition != position) {
            val newFragment: Fragment = supportFragmentManager.fragments[position]
            selectedPosition = position
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            for (fragment in supportFragmentManager.fragments) transaction.hide(fragment).setMaxLifecycle(fragment, STARTED)
            transaction.show(newFragment).setMaxLifecycle(newFragment, RESUMED).commitNowAllowingStateLoss()
        }
    }

    private fun initDrawer() {
        binding.navigationView.onNavigationSingleClick { item ->
            when (item.itemId) {
                R.id.oobe_dest -> {
                    startActivity(Intent(this@MainActivity, OOBEActivity::class.java))
                    @Suppress("DEPRECATION") if (SDK_INT < 34) overridePendingTransition(fade_in, fade_out)
                    finishAfterTransition()
                }

                R.id.about_app_dest -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.about_custom_dest -> startActivity(Intent(this, CustomAboutActivity::class.java))
                R.id.settings_dest -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.bottom_sheet_dest -> FragmentBottomSheet().show(supportFragmentManager, null)
                else -> return@onNavigationSingleClick false
            }
            true
        }
        binding.drawerLayout.setupHeaderAndNavRail(getString(R.string.about_app))
        binding.drawerLayout.setAppBarSuggestView(createSuggestAppBarModel())
        //binding.drawerLayout.isImmersiveScroll = true
    }

    private fun createSuggestAppBarModel(): SuggestAppBarModel<SuggestAppBarView> =
        SuggestAppBarModel.Builder(this).apply {
            setTitle("This is an a suggestion view")
            setCloseClickListener { _, _ -> binding.drawerLayout.setAppBarSuggestView(null) }
            setButtons(
                arrayListOf(ButtonModel(text = "Action Button", clickListener = { _, _ -> suggestiveSnackBar("Action button clicked!") }))
            )
        }.build()

    private fun initTabLayout() {
        binding.bottomTab.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_seek_bar -> startActivity(Intent(this@MainActivity, SeekBarActivity::class.java)).let { true }
                    R.id.menu_item_app_picker -> startActivity(Intent(this@MainActivity, AppPickerActivity::class.java)).let { true }
                    else -> false
                }
            }
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) = setFragment(tab.position)
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {
                    try {
                        when (tab.text) {
                            getString(R.string.design) -> findViewById<TabLayout>(R.id.fragmentDesignSubTabs).apply {
                                val newTabIndex = selectedTabPosition + 1
                                if (newTabIndex < tabCount) getTabAt(newTabIndex)?.select() else getTabAt(0)?.select()
                            }

                            getString(R.string.picker) -> binding.drawerLayout.setExpanded(!binding.drawerLayout.isExpanded, true)

                            getString(R.string.icons) -> findViewById<RecyclerView>(R.id.iconList).apply {
                                if (canScrollVertically(-1)) smoothScrollToPosition(0)
                                else binding.drawerLayout.setExpanded(!binding.drawerLayout.isExpanded, true)
                            }
                        }
                    } catch (e: Exception) { //no required functionality -> ignore errors
                        Log.e("MainActivity", "Error while reselecting tab", e)
                    }
                }
            })
        }
    }
}