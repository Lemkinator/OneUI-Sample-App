package de.lemke.oneuisample.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.ktx.onMultiClick
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.Failed
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.Loading
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.NoConnection
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.NoUpdate
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.NotUpdatable
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.Unset
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.UpdateAvailable
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.UpdateDownloaded
import de.lemke.oneuisample.BuildConfig.VERSION_NAME
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityAboutBinding
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.oneuiproject.oneui.design.R as designR

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appInfoLayout.apply {
            addOptionalText("Extra 1")
            addOptionalText("Extra 2")
            setMainButtonClickListener { suggestiveSnackBar("Main button clicked! updateState: $updateStatus") }
        }
        val version: TextView = binding.appInfoLayout.findViewById(designR.id.app_info_version)
        lifecycleScope.launch { setVersionTextView(version, getUserSettings().devModeEnabled) }
        version.onMultiClick {
            lifecycleScope.launch {
                val newDevModeEnabled = !getUserSettings().devModeEnabled
                updateUserSettings { it.copy(devModeEnabled = newDevModeEnabled) }
                setVersionTextView(version, newDevModeEnabled)
            }
        }
        binding.aboutButtonStatus.setOnClickListener { changeStatus() }
        binding.aboutButtonGithub.setOnClickListener { openGitHubPage() }
        binding.aboutButtonOpenSourceLicenses.setOnClickListener { startActivity(Intent(this, OssLicensesMenuActivity::class.java)) }
    }

    private fun setVersionTextView(textView: TextView, devModeEnabled: Boolean) {
        textView.text = getString(designR.string.oui_des_version_info, VERSION_NAME + if (devModeEnabled) " (dev)" else "")
    }

    fun changeStatus() {
        binding.appInfoLayout.updateStatus = when (binding.appInfoLayout.updateStatus) {
            Loading -> UpdateAvailable
            UpdateAvailable -> UpdateDownloaded
            UpdateDownloaded -> NoUpdate
            NoUpdate -> NotUpdatable
            NotUpdatable -> NoConnection
            NoConnection -> Failed("Failed!")
            Failed("Failed!") -> Unset
            else -> Loading
        }
    }

    fun openGitHubPage() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.link_oneui_design).toUri()))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace(); suggestiveSnackBar(getString(R.string.no_browser_app_installed)); false
        } catch (e: Exception) {
            e.printStackTrace(); suggestiveSnackBar(getString(R.string.error_cant_open_url)); false
        }
    }
}