package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.databinding.ActivitySeekBarBinding
import dev.oneuiproject.oneui.utils.SeekBarUtils

@AndroidEntryPoint
class SeekBarActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeekBarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekBarBinding.inflate(layoutInflater)
        binding.toolbarLayout.setNavigationButtonOnClickListener { finish() }
        setContentView(binding.root)

        SeekBarUtils.showTickMark(binding.fragmentSeekbar1, true)
        binding.fragmentSeekbar2.setOverlapPointForDualColor(70)
        SeekBarUtils.showOverlapPreview(binding.fragmentSeekbar2, true)
    }
}