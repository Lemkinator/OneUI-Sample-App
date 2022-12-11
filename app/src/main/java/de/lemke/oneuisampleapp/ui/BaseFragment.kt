package de.lemke.oneuisampleapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment(), FragmentInfo {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container, false)
    }
    abstract override val layoutResId: Int
    abstract override val iconResId: Int
    abstract override val title: CharSequence
    override val isAppBarEnabled: Boolean = true
}