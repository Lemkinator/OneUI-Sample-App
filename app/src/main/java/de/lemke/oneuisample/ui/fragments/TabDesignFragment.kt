package de.lemke.oneuisample.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.FragmentTabDesignBinding
import de.lemke.oneuisample.ui.util.autoCleared

class TabDesignFragment : AbsBaseFragment(R.layout.fragment_tab_design) {
    private val binding by autoCleared { FragmentTabDesignBinding.bind(requireView()) }
    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        view?.findViewById<ViewPager2>(R.id.viewPager2Design)?.adapter = null
        super.onDestroyView()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager2Design.adapter = TabDesignSubtabsAdapter(this)
        binding.viewPager2Design.seslSetSuggestionPaging(true)
        binding.viewPager2Design.offscreenPageLimit = 2
        binding.viewPager2Design.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                }

                override fun onPageSelected(position: Int) {}

                override fun onPageScrollStateChanged(state: Int) {}
            },
        )
        tabLayoutMediator = TabLayoutMediator(binding.fragmentDesignSubTabs, binding.viewPager2Design) { tab, position ->
            tab.text = arrayOf(getString(R.string.widgets), getString(R.string.progress_bar), getString(R.string.qr))[position]
        }.also { it.attach() }
    }
}

class TabDesignSubtabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> SubtabWidgetsFragment()
            1 -> SubtabProgressBarFragment()
            2 -> SubtabQrFragment()
            else -> SubtabWidgetsFragment()
        }
}
