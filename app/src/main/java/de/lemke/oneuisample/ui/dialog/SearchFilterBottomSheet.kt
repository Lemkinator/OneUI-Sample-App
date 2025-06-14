package de.lemke.oneuisample.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.databinding.BottomSheetSearchFilterBinding

@AndroidEntryPoint
class SearchFilterBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetSearchFilterBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            setOnShowListener { behavior.state = BottomSheetBehavior.STATE_EXPANDED }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        BottomSheetSearchFilterBinding.inflate(inflater, container, false).also { binding = it }.root
}