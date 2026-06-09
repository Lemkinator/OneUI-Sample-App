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
        if (Build.VERSION.SDK_INT >= 34) {
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
        collectEvents(viewModel.events) { event ->
            when (event) {
                OOBEEvent.NavigateToMain -> navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
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
        if (resources.configuration.screenWidthDp < 360) binding.oobeIntroFooterButton.layoutParams.width = MATCH_PARENT
        binding.oobeIntroFooterButton.setOnClickListener { viewModel.onAcceptTos() }
    }
}
