package de.lemke.oneuisample.ui

import android.R.anim.fade_in
import android.R.anim.fade_out
import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
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
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.fragments.FragmentBottomSheet
import de.lemke.oneuisample.ui.fragments.TabDesign
import de.lemke.oneuisample.ui.fragments.TabIcons
import de.lemke.oneuisample.ui.fragments.TabPicker
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.onSingleClick
import dev.oneuiproject.oneui.layout.DrawerLayout.DrawerState.CLOSE
import dev.oneuiproject.oneui.layout.DrawerLayout.DrawerState.CLOSING
import dev.oneuiproject.oneui.layout.DrawerLayout.DrawerState.OPEN
import dev.oneuiproject.oneui.layout.DrawerLayout.DrawerState.OPENING
import dev.oneuiproject.oneui.layout.NavDrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.oneuiproject.oneui.R as iconsR

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerListView: LinearLayout
    private var selectedPosition = -1
    private var time: Long = 0
    private var isUIReady = false
    private val drawerItemTitles: MutableList<TextView> = mutableListOf()

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

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
        listOf(TabDesign(), TabPicker(), TabIcons()).forEach {
            transaction.add(R.id.fragmentContainer, it)
        }
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
        drawerListView = findViewById(R.id.drawerListView)
        drawerItemTitles.apply {
            clear()
            add(findViewById(R.id.drawerItemOOBETitle))
            add(findViewById(R.id.drawerItemAboutAppTitle))
            add(findViewById(R.id.drawerItemCustomAboutAppTitle))
            add(findViewById(R.id.drawerItemSettingsTitle))
        }
        findViewById<LinearLayout>(R.id.drawerItemOOBE).onSingleClick {
            startActivity(Intent(this@MainActivity, OOBEActivity::class.java))
            @Suppress("DEPRECATION") if (SDK_INT < 34) overridePendingTransition(fade_in, fade_out)
            finishAfterTransition()
        }
        findViewById<LinearLayout>(R.id.drawerItemAboutApp).onSingleClick { startActivity(Intent(this, AboutActivity::class.java)) }
        findViewById<LinearLayout>(R.id.drawerItemCustomAboutApp).onSingleClick {
            startActivity(Intent(this, CustomAboutActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.drawerItemSettings).onSingleClick { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.drawerLayout.apply {
            setupHeaderButton(
                icon = AppCompatResources.getDrawable(context, iconsR.drawable.ic_oui_info_outline)!!,
                tooltipText = getString(R.string.about_app),
                listener = { startActivity(Intent(this@MainActivity, AboutActivity::class.java)) }
            )
            setNavRailContentMinSideMargin(14)
            setAppBarSuggestView(createSuggestAppBarModel())
            closeNavRailOnBack = true
            //isImmersiveScroll = true
            //setupNavRailFadeEffect
            if (isLargeScreenMode) {
                setDrawerStateListener {
                    when (it) {
                        OPEN -> offsetUpdaterJob?.cancel().also { updateOffset(1f) }
                        CLOSE -> offsetUpdaterJob?.cancel().also { updateOffset(0f) }
                        CLOSING, OPENING -> startOffsetUpdater()
                    }
                }
                //Set initial offset
                post { updateOffset(drawerOffset) }
            }
        }
    }

    private var offsetUpdaterJob: Job? = null
    private fun NavDrawerLayout.startOffsetUpdater() {
        //Ensure no duplicate job is running
        if (offsetUpdaterJob?.isActive == true) return
        offsetUpdaterJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                updateOffset(drawerOffset)
                delay(50)
            }
        }
    }

    fun updateOffset(offset: Float) {
        drawerItemTitles.forEach { it.alpha = offset }
        drawerListView.children.forEach {
            it.post {
                if (offset == 0f) {
                    it.updateLayoutParams<MarginLayoutParams> {
                        width = if (it is LinearLayout) 52f.dpToPx(it.context.resources) //drawer item
                        else 25f.dpToPx(it.context.resources) //divider item
                    }
                } else if (it.width != MATCH_PARENT) {
                    it.updateLayoutParams<MarginLayoutParams> { width = MATCH_PARENT }
                }
            }
        }
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
                    R.id.menu_item_bottom_sheet -> FragmentBottomSheet().show(supportFragmentManager, null).let { true }
                    R.id.menu_item_app_picker -> startActivity(Intent(this@MainActivity, AppPickerActivity::class.java)).let { true }
                    else -> false
                }
            }
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    setFragment(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {
                    try {
                        when (tab.text) {
                            getString(R.string.design) -> {
                                val subTabs: TabLayout = findViewById(R.id.fragmentDesignSubTabs)
                                val newTabIndex = subTabs.selectedTabPosition + 1
                                if (newTabIndex < subTabs.tabCount) subTabs.getTabAt(newTabIndex)?.select()
                                else subTabs.getTabAt(0)?.select()
                            }

                            getString(R.string.picker) -> binding.drawerLayout.setExpanded(!binding.drawerLayout.isExpanded, true)

                            getString(R.string.icons) -> {
                                val iconsRecyclerView: RecyclerView = findViewById(R.id.iconList)
                                if (iconsRecyclerView.canScrollVertically(-1)) iconsRecyclerView.smoothScrollToPosition(0)
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