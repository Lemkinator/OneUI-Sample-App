package de.lemke.oneuisample.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.BuildConfig.VERSION_NAME
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityAboutBinding
import de.lemke.oneuisample.ui.util.collectState
import de.lemke.oneuisample.ui.util.openURL
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.ktx.onMultiClick
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.Failed
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.Loading
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.NoConnection
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.NoUpdate
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.NotUpdatable
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.Unset
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.UpdateAvailable
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status.UpdateDownloaded
import dev.oneuiproject.oneui.design.R as designR

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    private val viewModel: AboutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appInfoLayout.apply {
            addOptionalText("Extra 1")
            addOptionalText("Extra 2")
            setMainButtonClickListener { suggestiveSnackBar("Main button clicked! updateState: $updateStatus") }
        }
        binding.appInfoLayout.findViewById<TextView>(designR.id.app_info_version).onMultiClick {
            viewModel.onToggleDevMode()
        }
        binding.aboutButtonStatus.setOnClickListener { changeStatus() }
        binding.aboutButtonGithub.setOnClickListener { openURL(getString(R.string.link_oneui_design)) }
        binding.aboutButtonOpenSourceLicenses.setOnClickListener { startActivity(Intent(this, LibsActivity::class.java)) }
        collectState(viewModel.state) { render(it) }
    }

    private fun render(state: AboutUiState) {
        binding.appInfoLayout.findViewById<TextView>(designR.id.app_info_version).text =
            getString(designR.string.oui_des_version_info, VERSION_NAME + if (state.devModeEnabled) " (dev)" else "")
    }

    fun changeStatus() {
        binding.appInfoLayout.updateStatus =
            when (binding.appInfoLayout.updateStatus) {
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
}
