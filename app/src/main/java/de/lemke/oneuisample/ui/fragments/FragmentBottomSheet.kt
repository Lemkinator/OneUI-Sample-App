package de.lemke.oneuisample.ui.fragments

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import dev.oneuiproject.oneui.app.SemBottomSheetDialogFragment

@AndroidEntryPoint
class FragmentBottomSheet : SemBottomSheetDialogFragment(R.layout.fragment_bottom_sheet) {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            setOnShowListener { behavior.state = STATE_EXPANDED }
        }
}
