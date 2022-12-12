package de.lemke.oneuisampleapp.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.databinding.ActivityAboutBinding
import de.lemke.oneuisampleapp.domain.GetUserSettingsUseCase
import de.lemke.oneuisampleapp.domain.UpdateUserSettingsUseCase
import dev.oneuiproject.oneui.layout.AppInfoLayout
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
        val version: View = binding.appInfoLayout.findViewById(dev.oneuiproject.oneui.design.R.id.app_info_version)
        version.setOnClickListener {
            clicks++
            if (clicks > 5) {
                clicks = 0
                lifecycleScope.launch {
                    val newDevModeEnabled = !getUserSettings().devModeEnabled
                    updateUserSettings { it.copy(devModeEnabled = newDevModeEnabled) }
                }
                startActivity(Intent().setClass(applicationContext, SplashActivity::class.java))
            }
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