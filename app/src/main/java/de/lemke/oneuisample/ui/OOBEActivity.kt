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

import android.R.anim.fade_in
import android.R.anim.fade_out
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.LayoutParams
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityOobeBinding
import de.lemke.oneuisample.ui.util.collectEvents
import de.lemke.oneuisample.ui.util.collectState
import de.lemke.oneuisample.ui.util.finishWithFade
import dev.oneuiproject.oneui.widget.OnboardingTipsItemView

@AndroidEntryPoint
class OOBEActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOobeBinding
    private val viewModel: OOBEViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, fade_in, fade_out)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, fade_in, fade_out)
        }
        binding = ActivityOobeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setTitle(getString(R.string.app_name))
        initTipsItems()
        initToSView()
        initFooterButton()
        collectState(viewModel.isAccepting) { isAccepting ->
            binding.oobeIntroFooterTosText.isEnabled = !isAccepting
            binding.oobeIntroFooterButton.isVisible = !isAccepting
            binding.oobeIntroFooterButtonProgress.isVisible = isAccepting
        }
        collectEvents(viewModel.events) { handleOOBEEvent(it) }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun handleOOBEEvent(event: OOBEEvent) {
        when (event) {
            OOBEEvent.NavigateToMain -> navigateToMain()
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishWithFade()
    }

    private fun initTipsItems() {
        val tipsData =
            listOf(
                Triple(R.string.oobe_onboard_msg1_title, R.string.oobe_onboard_msg1_summary, R.drawable.oobe1_icon),
                Triple(R.string.oobe_onboard_msg2_title, R.string.oobe_onboard_msg2_summary, R.drawable.oobe2_icon),
                Triple(R.string.oobe_onboard_msg3_title, R.string.oobe_onboard_msg3_summary, R.drawable.oobe3_icon),
            )
        tipsData.forEach { (titleRes, summaryRes, iconRes) ->
            OnboardingTipsItemView(this).apply {
                setIcon(iconRes)
                title = getString(titleRes)
                summary = getString(summaryRes)
                binding.oobeIntroTipsContainer.addView(this, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            }
        }
    }

    private fun initToSView() {
        val tos = getString(R.string.tos)
        val tosText = getString(R.string.oobe_tos_text, tos)
        val tosIndex = tosText.lastIndexOf(tos)
        binding.oobeIntroFooterTosText.text =
            SpannableString(tosText).apply {
                setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            AlertDialog
                                .Builder(this@OOBEActivity)
                                .setTitle(getString(R.string.tos))
                                .setMessage(getString(R.string.tos_content))
                                .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                                .show()
                        }
                    },
                    tosIndex,
                    tosIndex + tos.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        binding.oobeIntroFooterTosText.movementMethod = LinkMovementMethod.getInstance()
        binding.oobeIntroFooterTosText.highlightColor = Color.TRANSPARENT
    }

    private fun initFooterButton() {
        if (resources.configuration.screenWidthDp < MIN_FULL_BUTTON_WIDTH_DP) {
            binding.oobeIntroFooterButton.layoutParams.width = MATCH_PARENT
        }
        binding.oobeIntroFooterButton.setOnClickListener { viewModel.onAcceptTos() }
    }

    companion object {
        private const val MIN_FULL_BUTTON_WIDTH_DP = 360
    }
}
