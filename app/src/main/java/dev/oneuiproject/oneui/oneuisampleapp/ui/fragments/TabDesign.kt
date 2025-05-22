package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabDesignBinding

@AndroidEntryPoint
class TabDesign : Fragment() {
    private lateinit var binding: FragmentTabDesignBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentTabDesignBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager2Design.adapter = ViewPager2AdapterTabDesignSubtabs(this)
        binding.viewPager2Design.seslSetSuggestionPaging(true)
        binding.viewPager2Design.offscreenPageLimit = 2
        binding.viewPager2Design.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        TabLayoutMediator(binding.fragmentDesignSubTabs, binding.viewPager2Design) { tab, position ->
            tab.text = arrayOf(getString(R.string.widgets), getString(R.string.progress_bar), getString(R.string.qr))[position]
        }.attach()
    }
}


class ViewPager2AdapterTabDesignSubtabs(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> SubtabWidgets()
        1 -> SubtabProgressBar()
        2 -> SubtabQR()
        else -> SubtabWidgets()
    }
}