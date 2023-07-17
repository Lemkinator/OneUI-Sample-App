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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TabDesignSubtabProgressBar : Fragment() {
    private lateinit var binding: FragmentTabDesignSubtabProgressBarBinding
    private var animateProgressJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabDesignSubtabProgressBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (progressBar in listOf(
            binding.fragmentProgressbar1,
            binding.fragmentProgressbar2,
            binding.fragmentProgressbar3,
            binding.fragmentProgressbar4,
        )) {
            progressBar.setMode(SeslProgressBar.MODE_CIRCLE)
            progressBar.progress = 0
            progressBar.max = 1000
        }
        binding.fragmentProgressbar5.progress = 0
        binding.fragmentProgressbar5.max = 1000
    }

    override fun onPause() {
        super.onPause()
        animateProgressJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        animateProgressJob = lifecycleScope.launch {
            while (true) {
                for (progressBar in listOf(
                    binding.fragmentProgressbar1,
                    binding.fragmentProgressbar2,
                    binding.fragmentProgressbar3,
                    binding.fragmentProgressbar4,
                    binding.fragmentProgressbar5,
                )) {
                    progressBar.progress = (progressBar.progress + 1) % 1000
                }
                kotlinx.coroutines.delay(10)
            }
        }
    }
}