package de.lemke.oneuisample.ui.fragments

import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.R
import dev.oneuiproject.oneui.app.SemBottomSheetDialogFragment

@AndroidEntryPoint
class BottomSheetFragment : SemBottomSheetDialogFragment(R.layout.fragment_bottom_sheet) {
    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = STATE_EXPANDED
        }
    }
}
