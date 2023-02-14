package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabDesignSubtabWidgetsBinding

@AndroidEntryPoint
class TabDesignSubtabWidgets : Fragment() {
    private lateinit var binding: FragmentTabDesignSubtabWidgetsBinding
    private val faceJsons = listOf("Great_Face_Icon.json", "Good_Face_Icon.json", "Checking_Face_Icon.json", "Issues_found_Face_Icon.json")
    private val faceJsonNames = listOf("Great Face", "Good Face", "Checking Face", "Issues found Face")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabDesignSubtabWidgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentBtn1.setOnClickListener { Toast.makeText(context, "Button 1 clicked", Toast.LENGTH_SHORT).show() }
        binding.fragmentBtn2.setOnClickListener { Toast.makeText(context, "Button 2 clicked", Toast.LENGTH_SHORT).show() }
        binding.fragmentBtn3.setOnClickListener { Toast.makeText(context, "Button 3 clicked", Toast.LENGTH_SHORT).show() }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, faceJsonNames)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        binding.fragmentSpinner.adapter = adapter
        binding.fragmentSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.faceIconLottie.setAnimation(faceJsons[position])
                binding.faceIconLottie.addValueCallback(
                    KeyPath("**"),
                    LottieProperty.COLOR_FILTER,
                    LottieValueCallback(SimpleColorFilter(requireContext().getColor(R.color.primary_color_themed)))
                )
                binding.faceIconLottie.playAnimation()
            }
        })
    }
}