package de.lemke.oneuisample.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.databinding.FragmentBottomSheetBinding

@AndroidEntryPoint
class FragmentBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBottomSheetBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            setOnShowListener { behavior.state = STATE_EXPANDED }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentBottomSheetBinding.inflate(inflater, container, false).also { binding = it }.root
}