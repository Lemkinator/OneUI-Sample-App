package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.ktx.invokeOnBack
import dev.oneuiproject.oneui.ktx.isInMultiWindowModeCompat
import dev.oneuiproject.oneui.ktx.semSetToolTipText
import dev.oneuiproject.oneui.ktx.setEnableRecursive
import dev.oneuiproject.oneui.oneuisampleapp.BuildConfig.APPLICATION_ID
import dev.oneuiproject.oneui.oneuisampleapp.BuildConfig.VERSION_NAME
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.ActivityCustomAboutBinding
import dev.oneuiproject.oneui.oneuisampleapp.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.utils.DeviceLayoutUtil.isPortrait
import dev.oneuiproject.oneui.widget.AdaptiveCoordinatorLayout.Companion.MARGIN_PROVIDER_ADP_DEFAULT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

@AndroidEntryPoint
class CustomAboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomAboutBinding
    private val appBarListener = AboutAppBarListener()
    private val progressInterpolator = PathInterpolatorCompat.create(0f, 0f, 0f, 1f)
    private val callbackIsActive = MutableStateFlow(false)
    private var isBackProgressing = false
    private var isExpanding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomAboutBinding.inflate(layoutInflater)
        binding.root.configureAdaptiveMargin(MARGIN_PROVIDER_ADP_DEFAULT, binding.aboutBottomContainer)
        setContentView(binding.root)
        applyInsetIfNeeded()
        setupToolbar()

        initContent()
        refreshAppBar(resources.configuration)
        setupOnClickListeners()
        initOnBackPressed()
    }

    private fun applyInsetIfNeeded() {
        if (SDK_INT >= 30 && !window.decorView.fitsSystemWindows) {
            binding.root.setOnApplyWindowInsetsListener { _, insets ->
                val systemBarsInsets = insets.getInsets(systemBars())
                binding.root.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom)
                insets
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.aboutToolbar)
        //Should be called after setSupportActionBar
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
            onBackProgressed = {
                val interpolatedProgress = progressInterpolator.getInterpolation(it.progress)
                if (interpolatedProgress > .5 && !isExpanding) {
                    isExpanding = true
                    binding.aboutAppBar.setExpanded(true, true)
                } else if (interpolatedProgress < .3 && isExpanding) {
                    isExpanding = false
                    binding.aboutAppBar.setExpanded(false, true)
                }
            },
            onBackCancelled = {
                binding.aboutAppBar.setExpanded(false)
                isBackProgressing = false
                isExpanding = false
            }
        )
        updateCallbackState()
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
            val intent = Intent(
                "android.settings.APPLICATION_DETAILS_SETTINGS",
                Uri.fromParts("package", APPLICATION_ID, null)
            )
            intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return true
        }
        return false
    }

    @SuppressLint("RestrictedApi")
    private fun refreshAppBar(config: Configuration) {
        if (config.orientation != ORIENTATION_LANDSCAPE && !isInMultiWindowModeCompat) {
            binding.aboutAppBar.apply {
                seslSetCustomHeightProportion(true, 0.5f)//expanded
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

    fun openURL(url: String) = try {
        startActivity(Intent(ACTION_VIEW, url.toUri()))
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        suggestiveSnackBar(getString(R.string.no_browser_app_installed))
        false
    } catch (e: Exception) {
        e.printStackTrace()
        suggestiveSnackBar(getString(R.string.error_cant_open_url))
        false
    }

    private inner class AboutAppBarListener : OnOffsetChangedListener {
        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
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
                binding.aboutSwipeUpContainer.alpha = (1 - offsetAlpha * -3).coerceIn(0f, 1f)
            }
            // Handle the bottom part of the UI
            val alphaRange = binding.aboutCTL.height * 0.143f
            val layoutPosition = abs(appBarLayout.top).toFloat()
            val bottomAlpha = (150.0f / alphaRange * (layoutPosition - binding.aboutCTL.height * 0.35f)).coerceIn(0f, 255f)
            binding.aboutBottomContainer.alpha = bottomAlpha / 255
            updateCallbackState(appBarLayout.getTotalScrollRange() + verticalOffset == 0)
        }
    }

    private fun updateCallbackState(enable: Boolean? = null) {
        if (isBackProgressing) return
        callbackIsActive.value =
            (enable ?: (binding.aboutAppBar.seslIsCollapsed() && isPortrait(resources.configuration) && !isInMultiWindowModeCompat))
    }
}