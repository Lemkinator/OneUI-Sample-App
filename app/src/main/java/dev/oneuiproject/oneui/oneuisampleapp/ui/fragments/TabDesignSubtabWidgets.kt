package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabDesignSubtabWidgetsBinding

@AndroidEntryPoint
class TabDesignSubtabWidgets : Fragment() {
    private lateinit var binding: FragmentTabDesignSubtabWidgetsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabDesignSubtabWidgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentBtn1.setOnClickListener { Toast.makeText(context, "Button 1 clicked", Toast.LENGTH_SHORT).show() }
        binding.fragmentBtn2.setOnClickListener { Toast.makeText(context, "Button 2 clicked", Toast.LENGTH_SHORT).show() }
        binding.fragmentBtn3.setOnClickListener { Toast.makeText(context, "Button 3 clicked", Toast.LENGTH_SHORT).show() }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Spinner Item 1", "Spinner Item 2", "Spinner Item 3", "Spinner Item 4")
        )
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        binding.fragmentSpinner.adapter = adapter
    }
}