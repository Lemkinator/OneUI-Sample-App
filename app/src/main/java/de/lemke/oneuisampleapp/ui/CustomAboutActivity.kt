package de.lemke.oneuisampleapp.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import com.google.android.material.appbar.AppBarLayout
import de.lemke.oneuisampleapp.BuildConfig
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.databinding.ActivityCustomAboutBinding
import de.lemke.oneuisampleapp.databinding.ActivityCustomAboutContentBinding
import dev.oneuiproject.oneui.utils.ViewUtils
import dev.oneuiproject.oneui.utils.internal.ToolbarLayoutUtils
import dev.oneuiproject.oneui.widget.Toast
import kotlin.math.abs

class CustomAboutActivity : AppCompatActivity(), View.OnClickListener {
    private var enablebacktoheader = false
    private var lastClickTime: Long = 0
    private lateinit var binding: ActivityCustomAboutBinding
    private lateinit var bottomContent: ActivityCustomAboutContentBinding
    private val appBarListener = AboutAppBarListener()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomContent = binding.aboutBottomContent
        setSupportActionBar(binding.aboutToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.aboutToolbar.setNavigationOnClickListener { finish() }
        resetAppBar(resources.configuration)
        initContent()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resetAppBar(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_app_info) {
            val intent = Intent(
                "android.settings.APPLICATION_DETAILS_SETTINGS",
                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return true
        }
        return false
    }

    @SuppressLint("RestrictedApi")
    private fun resetAppBar(config: Configuration) {
        ToolbarLayoutUtils.hideStatusBarForLandscape(this, config.orientation)
        ToolbarLayoutUtils.updateListBothSideMargin(this, binding.aboutBottomContainer)
        if (config.orientation != Configuration.ORIENTATION_LANDSCAPE && !isInMultiWindowMode) {
            binding.aboutAppBar.seslSetCustomHeightProportion(true, 0.5f)
            enablebacktoheader = true
            binding.aboutAppBar.addOnOffsetChangedListener(appBarListener)
            binding.aboutAppBar.setExpanded(true, false)
            binding.aboutSwipeUpContainer.visibility = View.VISIBLE
            val lp: ViewGroup.LayoutParams = binding.aboutSwipeUpContainer.layoutParams
            lp.height = resources.displayMetrics.heightPixels / 2
        } else {
            binding.aboutAppBar.setExpanded(false, false)
            enablebacktoheader = false
            binding.aboutAppBar.seslSetCustomHeightProportion(true, 0F)
            binding.aboutAppBar.removeOnOffsetChangedListener(appBarListener)
            binding.aboutBottomContainer.alpha = 1f
            setBottomContentEnabled(true)
            binding.aboutSwipeUpContainer.visibility = View.GONE
        }
    }

    private fun initContent() {
        ViewUtils.semSetRoundedCorners(
            binding.aboutBottomContent.root,
            ViewUtils.SEM_ROUNDED_CORNER_TOP_LEFT or ViewUtils.SEM_ROUNDED_CORNER_TOP_RIGHT
        )
        ViewUtils.semSetRoundedCornerColor(
            binding.aboutBottomContent.root,
            ViewUtils.SEM_ROUNDED_CORNER_TOP_LEFT or ViewUtils.SEM_ROUNDED_CORNER_TOP_RIGHT,
            getColor(dev.oneuiproject.oneui.design.R.color.oui_round_and_bgcolor)
        )
        val appIcon = getDrawable(R.drawable.ic_launcher)
        binding.aboutHeaderAppIcon.setImageDrawable(appIcon)
        binding.aboutBottomAppIcon.setImageDrawable(appIcon)
        binding.aboutHeaderAppVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        binding.aboutBottomAppVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        binding.aboutHeaderGithub.setOnClickListener(this)
        TooltipCompat.setTooltipText(binding.aboutHeaderGithub, "GitHub")
        binding.aboutHeaderTelegram.setOnClickListener(this)
        TooltipCompat.setTooltipText(binding.aboutHeaderTelegram, "Telegram")
        bottomContent.aboutBottomDevYann.setOnClickListener(this)
        bottomContent.aboutBottomDevMesa.setOnClickListener(this)
        bottomContent.aboutBottomOssApache.setOnClickListener(this)
        bottomContent.aboutBottomOssMit.setOnClickListener(this)
        bottomContent.aboutBottomRelativeJetpack.setOnClickListener(this)
        bottomContent.aboutBottomRelativeMaterial.setOnClickListener(this)
        bottomContent.aboutBottomRelativeOuip.setOnClickListener(this)
    }

    private fun setBottomContentEnabled(enabled: Boolean) {
        binding.aboutHeaderGithub.isEnabled = !enabled
        binding.aboutHeaderTelegram.isEnabled = !enabled
        bottomContent.aboutBottomDevYann.isEnabled = enabled
        bottomContent.aboutBottomDevMesa.isEnabled = enabled
        bottomContent.aboutBottomOssApache.isEnabled = enabled
        bottomContent.aboutBottomOssMit.isEnabled = enabled
        bottomContent.aboutBottomRelativeJetpack.isEnabled = enabled
        bottomContent.aboutBottomRelativeMaterial.isEnabled = enabled
        bottomContent.aboutBottomRelativeOuip.isEnabled = enabled
    }

    override fun onClick(v: View) {
        val uptimeMillis = SystemClock.uptimeMillis()
        if (uptimeMillis - lastClickTime > 600L) {
            var url: String? = null
            when (v.id) {
                binding.aboutHeaderGithub.id -> url = "https://github.com/OneUIProject/oneui-design"
                binding.aboutHeaderTelegram.id -> url = "https://t.me/oneuiproject"
                bottomContent.aboutBottomDevYann.id -> url = "https://github.com/Yanndroid"
                bottomContent.aboutBottomDevMesa.id -> url = "https://github.com/BlackMesa123"
                bottomContent.aboutBottomOssApache.id -> url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                bottomContent.aboutBottomOssMit.id -> url = "https://github.com/OneUIProject/sesl/blob/main/LICENSE"
                bottomContent.aboutBottomRelativeJetpack.id -> url = "https://developer.android.com/jetpack"
                bottomContent.aboutBottomRelativeMaterial.id -> url = "https://material.io/develop/android"
                bottomContent.aboutBottomRelativeOuip.id -> url = "https://github.com/OneUIProject"
            }
            if (url != null) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "No suitable activity found", Toast.LENGTH_SHORT).show()
                }
            }
        }
        lastClickTime = uptimeMillis
    }

    private inner class AboutAppBarListener : AppBarLayout.OnOffsetChangedListener {
        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            // Handle the SwipeUp anim view
            val totalScrollRange: Int = appBarLayout.totalScrollRange
            val abs = abs(verticalOffset)
            if (abs >= totalScrollRange / 2) {
                binding.aboutSwipeUpContainer.alpha = 0f
                setBottomContentEnabled(true)
            } else if (abs == 0) {
                binding.aboutSwipeUpContainer.alpha = 1f
                setBottomContentEnabled(false)
            } else {
                val offsetAlpha: Float = appBarLayout.y / totalScrollRange
                var arrowAlpha = 1 - offsetAlpha * -3
                if (arrowAlpha < 0) arrowAlpha = 0f
                else if (arrowAlpha > 1) arrowAlpha = 1f
                binding.aboutSwipeUpContainer.alpha = arrowAlpha
            }

            // Handle the bottom part of the UI
            val alphaRange: Float = binding.aboutCtl.height * 0.143f
            val layoutPosition: Float = abs(appBarLayout.top).toFloat()
            var bottomAlpha: Float = (150.0f / alphaRange * (layoutPosition - binding.aboutCtl.height * 0.35f))
            if (bottomAlpha < 0) bottomAlpha = 0f
            else if (bottomAlpha >= 255) bottomAlpha = 255f
            binding.aboutBottomContainer.alpha = bottomAlpha / 255
        }
    }
}