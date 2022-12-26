package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.ActivityAboutBinding
import dev.oneuiproject.oneui.oneuisampleapp.domain.GetUserSettingsUseCase
import dev.oneuiproject.oneui.oneuisampleapp.domain.UpdateUserSettingsUseCase
import dev.oneuiproject.oneui.layout.AppInfoLayout
import dev.oneuiproject.oneui.oneuisampleapp.BuildConfig
import dev.oneuiproject.oneui.widget.Toast
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    private var clicks = 0

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appInfoLayout.addOptionalText("Extra 1")
        binding.appInfoLayout.addOptionalText("Extra 2")
        binding.appInfoLayout.setMainButtonClickListener(object : AppInfoLayout.OnClickListener {
            override fun onUpdateClicked(v: View) {
                Toast.makeText(this@AboutActivity, "onUpdateClicked", Toast.LENGTH_SHORT).show()
            }

            override fun onRetryClicked(v: View) {
                Toast.makeText(this@AboutActivity, "onRetryClicked", Toast.LENGTH_SHORT).show()
            }
        })
        val version: TextView = binding.appInfoLayout.findViewById(dev.oneuiproject.oneui.design.R.id.app_info_version)
        lifecycleScope. launch { setVersionTextView(version, getUserSettings().devModeEnabled) }
        version.setOnClickListener {
            clicks++
            if (clicks > 5) {
                clicks = 0
                lifecycleScope.launch {
                    val newDevModeEnabled = !getUserSettings().devModeEnabled
                    updateUserSettings { it.copy(devModeEnabled = newDevModeEnabled) }
                    setVersionTextView(version, newDevModeEnabled)
                }
            }
        }
    }

    private fun setVersionTextView(textView: TextView, devModeEnabled: Boolean) {
        lifecycleScope.launch {
            textView.text = getString(
                dev.oneuiproject.oneui.design.R.string.version_info, BuildConfig.VERSION_NAME + if (devModeEnabled) " (dev)" else ""
            )
        }
    }

    @SuppressLint("WrongConstant")
    @Suppress("unused_parameter")
    fun changeStatus(v: View?) {
        binding.appInfoLayout.status = if (binding.appInfoLayout.status + 1 == 4) -1 else binding.appInfoLayout.status + 1
    }

    @Suppress("unused_parameter")
    fun openGitHubPage(v: View?) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.link_github_oneui_design))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No suitable activity found", Toast.LENGTH_SHORT).show()
        }
    }
}