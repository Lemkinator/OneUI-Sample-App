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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SeslProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State.RESUMED
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabProgressBarBinding
import de.lemke.oneuisample.ui.util.autoCleared
import de.lemke.oneuisample.ui.util.launchAndRepeatWithViewLifecycle
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SubtabProgressBarFragment : Fragment() {
    private val binding by autoCleared { FragmentTabDesignSubtabProgressBarBinding.bind(requireView()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentTabDesignSubtabProgressBarBinding.inflate(inflater, container, false).root

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        listOf(binding.progressbar1, binding.progressbar2, binding.progressbar3, binding.progressbar4)
            .forEach {
                it.setMode(SeslProgressBar.MODE_CIRCLE)
                it.progress = 0
                it.max = PROGRESS_MAX
            }
        binding.progressbar5.progress = 0
        binding.progressbar5.max = PROGRESS_MAX
        startProgressUpdater()
    }

    @NoCoverage
    private fun startProgressUpdater() {
        launchAndRepeatWithViewLifecycle(RESUMED) {
            while (true) {
                listOf(binding.progressbar1, binding.progressbar2, binding.progressbar3, binding.progressbar4, binding.progressbar5)
                    .forEach { bar -> bar.progress = (bar.progress + 1) % PROGRESS_MAX }
                delay(16.milliseconds)
            }
        }
    }

    companion object {
        private const val PROGRESS_MAX = 1_000
    }
}
