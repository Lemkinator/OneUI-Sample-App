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
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
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
    private lateinit var versionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        versionTextView = binding.appInfoLayout.findViewById(designR.id.app_info_version)
        binding.appInfoLayout.apply {
            addOptionalText(getString(R.string.extra_1))
            addOptionalText(getString(R.string.extra_2))
            setMainButtonClickListener { suggestiveSnackBar(getString(R.string.main_button_clicked, updateStatus)) }
        }
        versionTextView.onMultiClick { viewModel.onToggleDevMode() }
        binding.aboutButtonStatus.setOnClickListener { changeStatus() }
        binding.aboutButtonGithub.setOnClickListener { openURL(getString(R.string.link_oneui_design)) }
        binding.aboutButtonOpenSourceLicenses.setOnClickListener { startActivity(Intent(this, LibsActivity::class.java)) }
        collectState(viewModel.state) { render(it) }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun render(state: AboutUiState) {
        versionTextView.text =
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
                NoConnection -> Failed(getString(R.string.failed))
                is Failed -> Unset
                else -> Loading
            }
    }
}
