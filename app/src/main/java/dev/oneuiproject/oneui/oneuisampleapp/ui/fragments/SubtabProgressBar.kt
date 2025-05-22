package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SeslProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabDesignSubtabProgressBarBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubtabProgressBar : Fragment() {
    private lateinit var binding: FragmentTabDesignSubtabProgressBarBinding
    private var animateProgressJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentTabDesignSubtabProgressBarBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listOf(binding.progressbar1, binding.progressbar2, binding.progressbar3, binding.progressbar4)
            .forEach { it.setMode(SeslProgressBar.MODE_CIRCLE); it.progress = 0; it.max = 1000 }
        binding.progressbar5.progress = 0
        binding.progressbar5.max = 1000
    }

    override fun onPause() {
        super.onPause()
        animateProgressJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        animateProgressJob = lifecycleScope.launch {
            while (true) {
                listOf(binding.progressbar1, binding.progressbar2, binding.progressbar3, binding.progressbar4, binding.progressbar5)
                    .forEach { bar -> bar.progress = (bar.progress + 1) % 1000 }
                delay(10)
            }
        }
    }
}