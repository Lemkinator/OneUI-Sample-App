package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabDesignBinding

@AndroidEntryPoint
class MainActivityTabDesign : Fragment() {
    private lateinit var binding: FragmentTabDesignBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabDesignBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentDesignSubTabs.seslSetSubTabStyle()
        binding.fragmentDesignSubTabs.tabMode = TabLayout.SESL_MODE_WEIGHT_AUTO
        binding.viewPager2Design.adapter = ViewPager2AdapterTabDesignSubtabs(this)
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
        0 -> TabDesignSubtabWidgets()
        1 -> TabDesignSubtabProgressBar()
        2 -> TabDesignSubtabQR()
        else -> TabDesignSubtabWidgets()
    }
}