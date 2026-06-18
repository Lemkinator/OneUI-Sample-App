/*
 * Copyright 2024-2026 Leonard Lemke
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
package de.lemke.oneuisample.ui.fragments

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context.SEARCH_SERVICE
import android.graphics.ColorFilter
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabWidgetsBinding
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabWidgetsBinding.inflate
import de.lemke.oneuisample.ui.MainActivity
import de.lemke.oneuisample.ui.util.autoCleared
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.ktx.setEntries
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubtabWidgetsFragment : Fragment() {
    private val binding by autoCleared { FragmentTabDesignSubtabWidgetsBinding.bind(requireView()) }
    private val faceJsons = listOf("great_face.json", "good_face.json", "checking_face.json", "sad_face.json")
    private val faceJsonNames = listOf("Great Face", "Good Face", "Checking Face", "Sad Face")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflate(inflater, container, false).root

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonColored.setOnClickListener { suggestiveSnackBar("Colored") }
        binding.buttonFilled.setOnClickListener { suggestiveSnackBar("Filled") }
        binding.buttonTransparent.setOnClickListener { suggestiveSnackBar("Transparent") }
        binding.buttonTransparentColored.setOnClickListener { suggestiveSnackBar("Transparent Colored") }
        binding.buttonTransparentThemed.setOnClickListener { suggestiveSnackBar("Transparent Themed") }
        binding.cardItemView.setOnClickListener { suggestiveSnackBar("Card Item View Clicked") }
        binding.bottomTipView.setOnClickListener { suggestiveSnackBar("Bottom Tip View Clicked") }
        binding.bottomTipView.setOnLinkClickListener { suggestiveSnackBar("Bottom Tip View Link Clicked") }
        binding.relativeLink1.setOnClickListener { suggestiveSnackBar("Relative Link 1 Clicked") }
        binding.relativeLink2.setOnClickListener { suggestiveSnackBar("Relative Link 2 Clicked") }
        SwitchBarSetup().setup()
        binding.fragmentSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, faceJsonNames).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        binding.fragmentSpinner.setEntries(faceJsonNames) { position, _ ->
            position?.let {
                binding.faceIconLottie.setAnimation(faceJsons[position])
                val callback = LottieValueCallback<ColorFilter>(SimpleColorFilter(requireContext().getColor(R.color.primary_color_themed)))
                binding.faceIconLottie.addValueCallback(KeyPath("**"), COLOR_FILTER, callback)
                binding.faceIconLottie.playAnimation()
            }
        }
        binding.searchView.apply {
            val searchManager = requireContext().getSystemService(SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(ComponentName(requireContext(), MainActivity::class.java)))
            seslSetUpButtonVisibility(VISIBLE)
            seslSetOnUpButtonClickListener { suggestiveSnackBar("Search Up Button Clicked") }
        }
        if (SDK_INT >= Q) binding.root.seslSetGoToTopEnabled(true)
    }

    private inner class SwitchBarSetup {
        private var progressJob: Job? = null

        fun setup() {
            val switchBar = binding.switchBar
            switchBar.addOnSwitchChangeListener { _, _ ->
                switchBar.setProgressBarVisible(true)
                progressJob?.cancel()
                progressJob =
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(1.seconds)
                        switchBar.setProgressBarVisible(false)
                    }
            }
        }
    }
}
