/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.FragmentTabDesignBinding
import de.lemke.oneuisample.ui.util.autoCleared

private enum class DesignSubtab(
    @field:StringRes val title: Int,
    val create: () -> Fragment,
) {
    WIDGETS(R.string.widgets, ::SubtabWidgetsFragment),
    PROGRESS_BAR(R.string.progress_bar, ::SubtabProgressBarFragment),
    QR(R.string.qr, ::SubtabQrFragment),
}

class TabDesignFragment : AbsBaseFragment(R.layout.fragment_tab_design) {
    private val binding by autoCleared { FragmentTabDesignBinding.bind(requireView()) }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val pager = binding.viewPager2Design
        pager.adapter = TabDesignSubtabsAdapter(this)
        pager.seslSetSuggestionPaging(true)
        pager.offscreenPageLimit = 2
        val tabLayoutMediator =
            TabLayoutMediator(binding.fragmentDesignSubTabs, pager) { tab, position ->
                tab.text = getString(DesignSubtab.entries[position].title)
            }
        tabLayoutMediator.attach()
        viewLifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    tabLayoutMediator.detach()
                    pager.adapter = null
                }
            },
        )
    }
}

class TabDesignSubtabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = DesignSubtab.entries.size

    override fun createFragment(position: Int): Fragment = DesignSubtab.entries[position].create()
}
