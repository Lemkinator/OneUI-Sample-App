package de.lemke.oneuisample.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.ktx.updateDualColorRange
import de.lemke.oneuisample.databinding.ActivitySeekBarBinding

@AndroidEntryPoint
class SeekBarActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeekBarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.seekbarOverlap.updateDualColorRange(70)
        binding.seekbarLevelSeamless.setSeamless(true)
    }
}