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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.ActivityOobeBinding
import de.lemke.oneuisample.domain.UpdateUserSettingsUseCase
import dev.oneuiproject.oneui.widget.OnboardingTipsItemView
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import dev.oneuiproject.oneui.R as oneuiR

@AndroidEntryPoint
class OOBEActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOobeBinding

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 34) overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, fade_in, fade_out)
        binding = ActivityOobeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTipsItems()
        initToSView()
        initFooterButton()
    }

    private fun initTipsItems() {
        val tipsData = listOf(
            Triple(R.string.oobe_onboard_msg1_title, R.string.oobe_onboard_msg1_summary, oneuiR.drawable.ic_oui_palette),
            Triple(R.string.oobe_onboard_msg2_title, R.string.oobe_onboard_msg2_summary, oneuiR.drawable.ic_oui_credit_card_outline),
            Triple(R.string.oobe_onboard_msg3_title, R.string.oobe_onboard_msg3_summary, oneuiR.drawable.ic_oui_decline)
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
        val tosLink = SpannableString(tosText)
        tosLink.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    AlertDialog.Builder(this@OOBEActivity)
                        .setTitle(getString(R.string.tos))
                        .setMessage(getString(R.string.tos_content))
                        .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                        .show()
                }
            },
            tosText.indexOf(tos), tosText.length - if (Locale.getDefault().language == "de") 4 else 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.oobeIntroFooterTosText.text = tosLink
        binding.oobeIntroFooterTosText.movementMethod = LinkMovementMethod.getInstance()
        binding.oobeIntroFooterTosText.highlightColor = Color.TRANSPARENT
    }

    private fun initFooterButton() {
        if (resources.configuration.screenWidthDp < 360) {
            binding.oobeIntroFooterButton.layoutParams.width = MATCH_PARENT
        }
        binding.oobeIntroFooterButton.setOnClickListener {
            binding.oobeIntroFooterTosText.isEnabled = false
            binding.oobeIntroFooterButton.isVisible = false
            binding.oobeIntroFooterButtonProgress.isVisible = true
            lifecycleScope.launch {
                updateUserSettings { it.copy(tosAccepted = true) }
                startActivity(Intent(this@OOBEActivity, MainActivity::class.java))
                @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT < 34) overridePendingTransition(fade_in, fade_out)
                finishAfterTransition()
            }
        }
    }
}