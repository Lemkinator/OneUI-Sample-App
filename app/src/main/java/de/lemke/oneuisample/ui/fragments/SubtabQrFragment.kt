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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabQrBinding.inflate
import dev.oneuiproject.oneui.qr.app.QrScanConfig
import dev.oneuiproject.oneui.qr.app.QrScanContract

@AndroidEntryPoint
class SubtabQrFragment : Fragment() {
    private val qrScanLauncher = registerForActivityResult(QrScanContract()) { result -> onQrScanResult(result) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflate(inflater, container, false).root

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupMenuProvider()
    }

    @NoCoverage
    private fun setupMenuProvider() =
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater,
                ) = menuInflater.inflate(R.menu.qr_tab_menu, menu)

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean = onScanMenuItemSelected(menuItem)
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onQrScanResult(result: String?) {
        if (result == null) return
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.scan_result_title))
            setMessage(result)
            setPositiveButton(getString(R.string.ok), null)
            show()
        }
    }

    /** Triggers the real camera-backed [QrScanContract] flow — not exercised under Robolectric. */
    @NoCoverage
    internal fun onScanMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {
            R.id.menu_item_scan_qr -> qrScanLauncher.launch(QrScanConfig()).let { true }
            else -> false
        }
}
