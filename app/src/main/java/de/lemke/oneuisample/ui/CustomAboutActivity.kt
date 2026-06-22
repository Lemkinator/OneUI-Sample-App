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

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowInsets.Type.systemBars
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.BuildConfig.APPLICATION_ID
import de.lemke.oneuisample.BuildConfig.VERSION_NAME
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityCustomAboutBinding
import de.lemke.oneuisample.ui.util.openURL
import dev.oneuiproject.oneui.ktx.invokeOnBack
import dev.oneuiproject.oneui.ktx.isInMultiWindowModeCompat
import dev.oneuiproject.oneui.ktx.semSetToolTipText
import dev.oneuiproject.oneui.ktx.setEnableRecursive
import dev.oneuiproject.oneui.utils.DeviceLayoutUtil.isPortrait
import dev.oneuiproject.oneui.widget.AdaptiveCoordinatorLayout.Companion.MARGIN_PROVIDER_ADP_DEFAULT
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class CustomAboutActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCustomAboutBinding.inflate(layoutInflater) }

    @VisibleForTesting(otherwise = PRIVATE)
    internal val appBarListener: OnOffsetChangedListener = AboutAppBarListener()
    private val progressInterpolator = PathInterpolatorCompat.create(0f, 0f, 0f, 1f)

    @VisibleForTesting(otherwise = PRIVATE)
    internal val callbackIsActive = MutableStateFlow(false)
    private var isBackProgressing = false
    private var isExpanding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.configureAdaptiveMargin(MARGIN_PROVIDER_ADP_DEFAULT, binding.aboutBottomContainer)
        setContentView(binding.root)
        applyInsetIfNeeded()
        setupToolbar()

        initContent()
        refreshAppBar(resources.configuration)
        setupOnClickListeners()
        initOnBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshAppBar(newConfig)
        updateCallbackState()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_app_info) {
            val intent =
                Intent(
                    "android.settings.APPLICATION_DETAILS_SETTINGS",
                    Uri.fromParts("package", APPLICATION_ID, null),
                )
            intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return true
        }
        return false
    }

    @NoCoverage
    private fun applyInsetIfNeeded() {
        if (SDK_INT >= VERSION_CODES.R && !window.decorView.fitsSystemWindows) {
            binding.root.setOnApplyWindowInsetsListener { _, insets ->
                val systemBarsInsets = insets.getInsets(systemBars())
                binding.root.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom)
                insets
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.aboutToolbar)
        // Should be called after setSupportActionBar
        binding.aboutToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun initOnBackPressed() {
        invokeOnBack(
            triggerStateFlow = callbackIsActive,
            onBackPressed = {
                binding.aboutAppBar.setExpanded(true)
                isBackProgressing = false
                isExpanding = false
            },
            onBackStarted = { isBackProgressing = true },
            onBackProgressed = { applyBackProgress(it.progress) },
            onBackCancelled = {
                binding.aboutAppBar.setExpanded(false)
                isBackProgressing = false
                isExpanding = false
            },
        )
        updateCallbackState()
    }

    private fun applyBackProgress(progress: Float) {
        val interpolatedProgress = progressInterpolator.getInterpolation(progress)
        if (interpolatedProgress > 0.5f && !isExpanding) {
            isExpanding = true
            binding.aboutAppBar.setExpanded(true, true)
        } else if (interpolatedProgress < BACK_COLLAPSE_THRESHOLD && isExpanding) {
            isExpanding = false
            binding.aboutAppBar.setExpanded(false, true)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun refreshAppBar(config: Configuration) {
        if (config.orientation != ORIENTATION_LANDSCAPE && !isInMultiWindowModeCompat) {
            binding.aboutAppBar.apply {
                seslSetCustomHeightProportion(true, 0.5f) // expanded
                addOnOffsetChangedListener(appBarListener)
                setExpanded(true, false)
            }
            binding.aboutSwipeUpContainer.apply {
                updateLayoutParams { height = resources.displayMetrics.heightPixels / 2 }
                isVisible = true
            }
        } else {
            binding.aboutAppBar.apply {
                setExpanded(false, false)
                seslSetCustomHeightProportion(true, 0f)
                removeOnOffsetChangedListener(appBarListener)
            }
            binding.aboutBottomContainer.alpha = 1f
            binding.aboutSwipeUpContainer.isVisible = false
            setBottomContentEnabled(true)
        }
    }

    private fun initContent() {
        val appIcon = AppCompatResources.getDrawable(this, R.drawable.ic_launcher)
        binding.aboutHeaderIcon.setImageDrawable(appIcon)
        binding.aboutBottomIcon.setImageDrawable(appIcon)
        binding.aboutHeaderAppVersion.text = getString(R.string.version, VERSION_NAME)
        binding.aboutBottomAppVersion.text = getString(R.string.version, VERSION_NAME)
        binding.aboutHeaderGithub.semSetToolTipText(getString(R.string.github))
        binding.aboutHeaderTelegram.semSetToolTipText(getString(R.string.telegram))
    }

    private fun setBottomContentEnabled(enabled: Boolean) {
        binding.aboutHeaderGithub.isEnabled = !enabled
        binding.aboutHeaderTelegram.isEnabled = !enabled
        binding.aboutBottomContent.aboutBottomScrollView.setEnableRecursive(enabled)
    }

    private fun setupOnClickListeners() {
        binding.aboutHeaderGithub.setOnClickListener { openURL(getString(R.string.link_oneui_design)) }
        binding.aboutHeaderTelegram.setOnClickListener { openURL(getString(R.string.link_telegram_oneui)) }
        with(binding.aboutBottomContent) {
            aboutBottomDevYann.setOnClickListener { openURL(getString(R.string.link_github_yann)) }
            aboutBottomDevMesa.setOnClickListener { openURL(getString(R.string.link_github_salvo)) }
            aboutBottomDevTribalfs.setOnClickListener { openURL(getString(R.string.link_github_tribalfs)) }
            aboutBottomRelativeJetpack.setOnClickListener { openURL(getString(R.string.link_jetpack)) }
            aboutBottomRelativeMaterial.setOnClickListener { openURL(getString(R.string.link_material)) }
            aboutBottomRelativeSeslAndroidx.setOnClickListener { openURL(getString(R.string.link_sesl_androidx)) }
            aboutBottomRelativeSeslMaterial.setOnClickListener { openURL(getString(R.string.link_sesl_material)) }
            aboutBottomRelativeDesign.setOnClickListener { openURL(getString(R.string.link_oneui_design)) }
        }
    }

    private inner class AboutAppBarListener : OnOffsetChangedListener {
        override fun onOffsetChanged(
            appBarLayout: AppBarLayout,
            verticalOffset: Int,
        ) {
            // Handle the SwipeUp anim view
            val totalScrollRange = appBarLayout.totalScrollRange
            val abs = abs(verticalOffset)
            if (abs >= totalScrollRange / 2) {
                binding.aboutSwipeUpContainer.alpha = 0f
                setBottomContentEnabled(true)
            } else if (abs == 0) {
                binding.aboutSwipeUpContainer.alpha = 1f
                setBottomContentEnabled(false)
            } else {
                val offsetAlpha = appBarLayout.y / totalScrollRange
                binding.aboutSwipeUpContainer.alpha = (1 + offsetAlpha * SWIPE_UP_ALPHA_SPEED).coerceIn(0f, 1f)
            }
            // Handle the bottom part of the UI
            val layoutPosition = abs(appBarLayout.top).toFloat()
            binding.aboutBottomContainer.alpha =
                this@CustomAboutActivity.computeBottomAlpha(binding.aboutCTL.height, layoutPosition) / ALPHA_RANGE
            updateCallbackState(appBarLayout.getTotalScrollRange() + verticalOffset == 0)
        }
    }

    @VisibleForTesting
    internal fun computeBottomAlpha(
        ctlHeight: Int,
        layoutPosition: Float,
    ): Float {
        val alphaRange = ctlHeight * BOTTOM_ALPHA_RANGE_FRACTION
        return if (alphaRange > 0f) {
            (BOTTOM_ALPHA_SCALE / alphaRange * (layoutPosition - ctlHeight * BOTTOM_FADE_START_FRACTION))
                .coerceIn(0f, ALPHA_RANGE)
        } else {
            0f
        }
    }

    @NoCoverage
    private fun isCallbackEnabled(): Boolean =
        binding.aboutAppBar.seslIsCollapsed() && isPortrait(resources.configuration) && !isInMultiWindowModeCompat

    private fun updateCallbackState(enable: Boolean? = null) {
        if (isBackProgressing) return
        callbackIsActive.value = enable ?: isCallbackEnabled()
    }

    companion object {
        private const val BACK_COLLAPSE_THRESHOLD = 0.3f
        private const val SWIPE_UP_ALPHA_SPEED = 3f
        private const val BOTTOM_ALPHA_SCALE = 150f
        private const val BOTTOM_ALPHA_RANGE_FRACTION = 0.143f
        private const val BOTTOM_FADE_START_FRACTION = 0.35f
        private const val ALPHA_RANGE = 255f
    }
}
