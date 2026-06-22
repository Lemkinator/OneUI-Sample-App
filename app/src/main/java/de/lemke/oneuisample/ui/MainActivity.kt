/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui

import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.appbar.model.ButtonModel
import com.google.android.material.appbar.model.SuggestAppBarModel
import com.google.android.material.appbar.model.view.SuggestAppBarView
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.BuildConfig
import de.lemke.oneuisample.BuildConfig.FIRST_RUN_SKIPPABLE
import de.lemke.oneuisample.BuildConfig.VERSION_CODE
import de.lemke.oneuisample.BuildConfig.VERSION_NAME
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.databinding.ActivityMainBinding
import de.lemke.oneuisample.openLeakCanary
import de.lemke.oneuisample.ui.fragments.BottomSheetFragment
import de.lemke.oneuisample.ui.util.configureSplashScreen
import de.lemke.oneuisample.ui.util.finishWithFade
import de.lemke.oneuisample.ui.util.onNavigationSingleClick
import de.lemke.oneuisample.ui.util.onboardIfNeeded
import de.lemke.oneuisample.ui.util.setupHeaderAndNavRail
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.navigation.setupNavigation
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isUIReady = false

    @Inject
    lateinit var userSettings: UserSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        onboardIfNeeded(userSettings, VERSION_CODE, VERSION_NAME, allowSkip = FIRST_RUN_SKIPPABLE) ?: return
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureSplashScreen(splashScreen, binding.root) { !isUIReady }
        initDrawer()
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

    private fun initDrawer() {
        binding.drawerLayout.apply {
            setupHeaderAndNavRail(getString(R.string.about_app))
            setAppBarSuggestView(createSuggestAppBarModel())
            // isImmersiveScroll = true
            setupNavigation(binding.bottomTab, binding.navigationHost.getFragment())
        }
        binding.navigationView.findMenuItem(R.id.leaks_dest)!!.isVisible = BuildConfig.DEBUG
        binding.navigationView.onNavigationSingleClick { item -> onNavigationItemSelected(item) }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.oobe_dest -> openOOBEAndFinish()
            R.id.about_app_dest -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.about_custom_dest -> startActivity(Intent(this, CustomAboutActivity::class.java))
            R.id.settings_dest -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.bottom_sheet_dest -> BottomSheetFragment().show(supportFragmentManager, null)
            R.id.leaks_dest -> openLeakCanary(this)
            else -> return false
        }
        return true
    }

    private fun initPopupMenu() {
        binding.navigationView.findMenuItem(R.id.popup_menu)!!.setOnMenuItemClickListener { onPopupMenuItemClick() }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onPopupMenuItemClick(): Boolean {
        if (binding.drawerLayout.drawerOffset == 0f) {
            binding.drawerLayout.setDrawerOpen(true)
        } else {
            openPopupMenu()
        }
        return true
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun openDrawer() = binding.drawerLayout.setDrawerOpen(true)

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun openPopupMenu() {
        PopupMenu(this, binding.navigationView.findViewById(R.id.popup_menu)).apply {
            seslSetOverlapAnchor(false)
            setForceShowIcon(true)
            seslSetOffset(POPUP_MENU_OFFSET_X, 0)
            inflate(R.menu.menu_popup)
            setOnMenuItemClickListener { menuItem -> onPopupMenuItemClicked(menuItem) }
            show()
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onPopupMenuItemClicked(menuItem: MenuItem): Boolean {
        title = menuItem.title
        suggestiveSnackBar("${menuItem.title} clicked")
        return true
    }

    private fun createSuggestAppBarModel(): SuggestAppBarModel<SuggestAppBarView> =
        SuggestAppBarModel
            .Builder(this)
            .apply {
                setTitle(getString(R.string.suggestion_title))
                setCloseClickListener { _, _ -> binding.drawerLayout.setAppBarSuggestView(null) }
                setButtons(
                    arrayListOf(
                        ButtonModel(
                            text = getString(R.string.action_button),
                            clickListener = { _, _ -> onSuggestActionButtonClicked() },
                        ),
                    ),
                )
            }.build()

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onSuggestActionButtonClicked() {
        suggestiveSnackBar(getString(R.string.action_button_clicked))
    }

    companion object {
        private const val POPUP_MENU_OFFSET_X = 140
    }
}
