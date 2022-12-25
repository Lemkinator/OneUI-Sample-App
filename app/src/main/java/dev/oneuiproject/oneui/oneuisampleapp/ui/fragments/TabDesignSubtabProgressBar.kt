package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SeslProgressBar
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabDesignSubtabProgressBarBinding

@AndroidEntryPoint
class TabDesignSubtabProgressBar : Fragment() {
    private lateinit var binding: FragmentTabDesignSubtabProgressBarBinding

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
            progressBar.progress = 40
        }
    }
}