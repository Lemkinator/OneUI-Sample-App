package de.lemke.oneuisample.ui.fragments

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialElevationScale

abstract class AbsBaseFragment(@LayoutRes layoutResId: Int) : Fragment(layoutResId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFragmentTransitions()
    }

    private fun setupFragmentTransitions() {
        enterTransition = MaterialElevationScale(true)
        exitTransition = MaterialElevationScale(true)
        reenterTransition = MaterialElevationScale(false)
        returnTransition = MaterialElevationScale(false)
    }
}



