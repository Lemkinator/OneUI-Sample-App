package de.lemke.oneuisample.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SeslSwitchBar
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.userSettings
import de.lemke.oneuisample.databinding.ActivitySwitchbarBinding
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator

@AndroidEntryPoint
class SwitchBarActivity : AppCompatActivity(), ViewYTranslator by AppBarAwareYTranslator(), SeslSwitchBar.OnSwitchChangeListener {
    private lateinit var binding: ActivitySwitchbarBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwitchbarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val enabled = userSettings.sampleSwitchBar
        binding.root.switchBar.isChecked = enabled
        update(enabled)
        binding.root.switchBar.addOnSwitchChangeListener(this)
        binding.switchBarExample.translateYWithAppBar(binding.root.appBarLayout, this)
    }

    override fun onSwitchChanged(
        switchCompat: SwitchCompat,
        enabled: Boolean,
    ) {
        userSettings.sampleSwitchBar = enabled
        update(enabled)
    }

    private fun update(enabled: Boolean) {
        binding.root.switchBar.apply {
            setProgressBarVisible(true)
            postDelayed({ setProgressBarVisible(false) }, 1_000)
        }
        binding.lottie.apply {
            cancelAnimation()
            setAnimation(if (enabled) "good_face.json" else "sad_face.json")
            progress = 0f
            isVisible = true
            addValueCallback(KeyPath("**"), COLOR_FILTER, LottieValueCallback(SimpleColorFilter(getColor(R.color.primary_color_themed))))
            postDelayed({ playAnimation() }, 400)
        }
    }
}
