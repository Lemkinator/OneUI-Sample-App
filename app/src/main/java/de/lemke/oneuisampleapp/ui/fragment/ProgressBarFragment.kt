package de.lemke.oneuisampleapp.ui.fragment

import de.lemke.oneuisampleapp.ui.BaseFragment
import android.os.Bundle
import android.view.View
import de.lemke.oneuisampleapp.R
import androidx.appcompat.widget.SeslProgressBar

class ProgressBarFragment : BaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ids = listOf(
            R.id.fragment_progressbar_1,
            R.id.fragment_progressbar_2,
            R.id.fragment_progressbar_3,
            R.id.fragment_progressbar_4
        )
        for (id in ids) {
            val progressBar = view.findViewById<SeslProgressBar>(id)
            progressBar.setMode(SeslProgressBar.MODE_CIRCLE)
            progressBar.progress = 40
        }
    }

    override val layoutResId: Int = R.layout.fragment_progress_bar
    override val iconResId: Int = R.drawable.drawer_page_icon_progressbar
    override val title: CharSequence = "ProgressBar"
}