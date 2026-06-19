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

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
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
import de.lemke.oneuisample.databinding.ActivitySwitchbarBinding
import de.lemke.oneuisample.ui.util.collectState
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator

@AndroidEntryPoint
class SwitchBarActivity : AppCompatActivity(), ViewYTranslator by AppBarAwareYTranslator(), SeslSwitchBar.OnSwitchChangeListener {
    private lateinit var binding: ActivitySwitchbarBinding
    private val viewModel: SwitchBarViewModel by viewModels()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwitchbarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.switchBar.addOnSwitchChangeListener(this)
        binding.switchBarExample.translateYWithAppBar(binding.root.appBarLayout, this)
        collectState(viewModel.state) { render(it) }
    }

    override fun onSwitchChanged(
        switchCompat: SwitchCompat,
        enabled: Boolean,
    ) {
        viewModel.onSwitchChanged(enabled)
    }

    private fun render(state: SwitchBarUiState) {
        binding.root.switchBar.apply {
            if (isChecked != state.enabled) {
                removeOnSwitchChangeListener(this@SwitchBarActivity)
                isChecked = state.enabled
                addOnSwitchChangeListener(this@SwitchBarActivity)
            }
        }
        update(state.enabled)
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
