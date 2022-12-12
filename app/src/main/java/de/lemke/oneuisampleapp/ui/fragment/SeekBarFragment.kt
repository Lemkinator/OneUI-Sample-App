package de.lemke.oneuisampleapp.ui.fragment

import de.lemke.oneuisampleapp.ui.BaseFragment
import android.os.Bundle
import android.view.View
import dev.oneuiproject.oneui.widget.HapticSeekBar
import de.lemke.oneuisampleapp.R
import dev.oneuiproject.oneui.utils.SeekBarUtils
import androidx.appcompat.widget.SeslSeekBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeekBarFragment : BaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val seekbar1 = view.findViewById<HapticSeekBar>(R.id.fragment_seekbar_1)
        SeekBarUtils.showTickMark(seekbar1, true)
        val seekbar2 = view.findViewById<SeslSeekBar>(R.id.fragment_seekbar_2)
        seekbar2.setOverlapPointForDualColor(70)
        SeekBarUtils.showOverlapPreview(seekbar2, true)
    }

    override val layoutResId: Int = R.layout.fragment_seek_bar
    override val iconResId: Int = R.drawable.drawer_page_icon_seekbar
    override val title: CharSequence = "SeekBar"
    override val isAppBarEnabled: Boolean = false
}