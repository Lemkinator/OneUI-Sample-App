package de.lemke.oneuisample.ui

import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.appbar.model.ButtonModel
import com.google.android.material.appbar.model.SuggestAppBarModel
import com.google.android.material.appbar.model.view.SuggestAppBarView
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.BuildConfig
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityMainBinding
import de.lemke.oneuisample.domain.configureSplashScreen
import de.lemke.oneuisample.domain.finishWithFade
import de.lemke.oneuisample.domain.onNavigationSingleClick
import de.lemke.oneuisample.domain.onboardIfNeeded
import de.lemke.oneuisample.domain.overrideFadeOpenTransition
import de.lemke.oneuisample.domain.setupHeaderAndNavRail
import de.lemke.oneuisample.domain.suggestiveSnackBar
import de.lemke.oneuisample.ui.fragments.FragmentBottomSheet
import dev.oneuiproject.oneui.navigation.setupNavigation

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isUIReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        overrideFadeOpenTransition()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureSplashScreen(splashScreen, binding.root) { !isUIReady }
        if (!onboardIfNeeded(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME, allowSkip = BuildConfig.FIRST_RUN_SKIPPABLE)) return
        initNavigation()
        initDrawerLayout()
        initPopupMenu()
        isUIReady = true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ACTION_SEARCH) binding.drawerLayout.setSearchQueryFromIntent(intent)
    }

    private fun openOOBEAndFinish() {
        startActivity(Intent(this@MainActivity, OOBEActivity::class.java))
        finishWithFade()
    }

    private fun initNavigation() {
        binding.navigationView.onNavigationSingleClick { item ->
            when (item.itemId) {
                R.id.oobe_dest -> openOOBEAndFinish()
                R.id.about_app_dest -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.about_custom_dest -> startActivity(Intent(this, CustomAboutActivity::class.java))
                R.id.settings_dest -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.bottom_sheet_dest -> FragmentBottomSheet().show(supportFragmentManager, null)
                else -> return@onNavigationSingleClick false
            }
            true
        }
    }

    private fun initDrawerLayout() {
        binding.drawerLayout.apply {
            setupHeaderAndNavRail(getString(R.string.about_app))
            setAppBarSuggestView(createSuggestAppBarModel())
            // isImmersiveScroll = true
            setupNavigation(binding.bottomTab, binding.navigationHost.getFragment())
        }
    }

    private fun initPopupMenu() {
        binding.navigationView.findMenuItem(R.id.popup_menu)?.apply {
            setOnMenuItemClickListener {
                if (binding.drawerLayout.drawerOffset == 0f) {
                    binding.drawerLayout.setDrawerOpen(true)
                } else {
                    PopupMenu(this@MainActivity, binding.navigationView.findViewById(R.id.popup_menu)).apply {
                        seslSetOverlapAnchor(false)
                        setForceShowIcon(true)
                        seslSetOffset(140, 0)
                        inflate(R.menu.menu_popup)
                        setOnMenuItemClickListener { menuItem ->
                            title = menuItem.title
                            suggestiveSnackBar("${menuItem.title} clicked")
                            true
                        }
                        show()
                    }
                }
                true
            }
        }
    }

    private fun createSuggestAppBarModel(): SuggestAppBarModel<SuggestAppBarView> =
        SuggestAppBarModel
            .Builder(this)
            .apply {
                setTitle("This is an a suggestion view")
                setCloseClickListener { _, _ -> binding.drawerLayout.setAppBarSuggestView(null) }
                setButtons(
                    arrayListOf(
                        ButtonModel(
                            text = "Action Button",
                            clickListener = { _, _ ->
                                suggestiveSnackBar("Action button clicked!")
                            },
                        ),
                    ),
                )
            }.build()
}
