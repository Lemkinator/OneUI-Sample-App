package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SeslSwitchBar
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.ActivitySwitchbarBinding
import dev.oneuiproject.oneui.oneuisampleapp.domain.GetUserSettingsUseCase
import dev.oneuiproject.oneui.oneuisampleapp.domain.UpdateUserSettingsUseCase
import dev.oneuiproject.oneui.utils.internal.ReflectUtils
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class SwitchBarActivity : AppCompatActivity(), SeslSwitchBar.OnSwitchChangeListener {
    private lateinit var binding: ActivitySwitchbarBinding

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwitchbarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.switchBar.addOnSwitchChangeListener(this)
        binding.root.setNavigationButtonTooltip(getString(R.string.sesl_navigate_up))
        binding.root.setNavigationButtonOnClickListener { finishAfterTransition() }
        lifecycleScope.launch {
            val enabled = getUserSettings().sampleSwitchbar
            binding.root.switchBar.isChecked = enabled
            binding.lottie.setAnimation(if (enabled) "Good_Face_Icon.json" else "Issues_found_Face_Icon.json")
        }
        binding.lottie.cancelAnimation()
        binding.lottie.progress = 0f
        binding.lottie.visibility = View.VISIBLE
        binding.lottie.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(SimpleColorFilter(getColor(R.color.primary_color_themed)))
        )
        binding.lottie.postDelayed({ binding.lottie.playAnimation() }, 400)
        binding.root.appBarLayout.addOnOffsetChangedListener { layout: AppBarLayout, verticalOffset: Int ->
            val totalScrollRange = layout.totalScrollRange
            val inputMethodWindowVisibleHeight = ReflectUtils.genericInvokeMethod(
                InputMethodManager::class.java,
                getSystemService(INPUT_METHOD_SERVICE),
                "getInputMethodWindowVisibleHeight"
            ) as Int
            if (totalScrollRange != 0) binding.switchbarExample.translationY = (abs(verticalOffset) - totalScrollRange).toFloat() / 2.0f
            else binding.switchbarExample.translationY = (abs(verticalOffset) - inputMethodWindowVisibleHeight).toFloat() / 2.0f
        }
    }

    override fun onSwitchChanged(switchCompat: SwitchCompat, enabled: Boolean) {
        lifecycleScope.launch {
            updateUserSettings { it.copy(sampleSwitchbar = enabled) }
        }
        binding.lottie.cancelAnimation()
        binding.lottie.setAnimation(if (enabled) "Good_Face_Icon.json" else "Issues_found_Face_Icon.json")
        binding.lottie.progress = 0f
        binding.lottie.visibility = View.VISIBLE
        binding.lottie.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(SimpleColorFilter(getColor(R.color.primary_color_themed)))
        )
        binding.lottie.postDelayed({ binding.lottie.playAnimation() }, 400)
    }
}