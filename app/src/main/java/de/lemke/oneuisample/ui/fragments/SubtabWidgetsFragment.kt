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
package de.lemke.oneuisample.ui.fragments

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context.SEARCH_SERVICE
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabWidgetsBinding
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabWidgetsBinding.inflate
import de.lemke.oneuisample.ui.MainActivity
import de.lemke.oneuisample.ui.util.autoCleared
import de.lemke.oneuisample.ui.util.play
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
    private val faceJsonNames: List<String>
        get() =
            listOf(
                getString(R.string.face_great),
                getString(R.string.face_good),
                getString(R.string.face_checking),
                getString(R.string.face_sad),
            )
    private var progressJob: Job? = null

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
        binding.buttonColored.setOnClickListener { suggestiveSnackBar(getString(R.string.colored)) }
        binding.buttonFilled.setOnClickListener { suggestiveSnackBar(getString(R.string.filled)) }
        binding.buttonTransparent.setOnClickListener { suggestiveSnackBar(getString(R.string.transparent)) }
        binding.buttonTransparentColored.setOnClickListener { suggestiveSnackBar(getString(R.string.transparent_colored)) }
        binding.buttonTransparentThemed.setOnClickListener { suggestiveSnackBar(getString(R.string.transparent_themed)) }
        binding.cardItemView.setOnClickListener { suggestiveSnackBar(getString(R.string.card_item_view_clicked)) }
        binding.bottomTipView.setOnClickListener { suggestiveSnackBar(getString(R.string.bottom_tip_view_clicked)) }
        binding.bottomTipView.setOnLinkClickListener { suggestiveSnackBar(getString(R.string.bottom_tip_view_link_clicked)) }
        binding.relativeLink1.setOnClickListener { suggestiveSnackBar(getString(R.string.relative_link_1_clicked)) }
        binding.relativeLink2.setOnClickListener { suggestiveSnackBar(getString(R.string.relative_link_2_clicked)) }
        binding.switchBar.addOnSwitchChangeListener { _, _ -> onSwitchToggled() }
        binding.fragmentSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, faceJsonNames).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        binding.fragmentSpinner.setEntries(faceJsonNames) { position, _ ->
            position?.let {
                binding.faceIconLottie.play(faceJsons[position], delay = 0.seconds)
            }
        }
        binding.searchView.apply {
            val searchManager = requireContext().getSystemService(SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(ComponentName(requireContext(), MainActivity::class.java)))
            seslSetUpButtonVisibility(VISIBLE)
            seslSetOnUpButtonClickListener { suggestiveSnackBar(getString(R.string.search_up_button_clicked)) }
        }
        if (SDK_INT >= Q) binding.root.seslSetGoToTopEnabled(true)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onSwitchToggled() {
        binding.switchBar.setProgressBarVisible(true)
        progressJob?.cancel()
        progressJob =
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1.seconds)
                binding.switchBar.setProgressBarVisible(false)
            }
    }
}
