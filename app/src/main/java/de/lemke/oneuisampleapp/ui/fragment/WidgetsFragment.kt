package de.lemke.oneuisampleapp.ui.fragment

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SearchView
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.ui.BaseFragment
import de.lemke.oneuisampleapp.ui.MainActivity

class WidgetsFragment : BaseFragment(), View.OnClickListener {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<AppCompatButton>(R.id.fragment_btn_1).setOnClickListener(this)
        view.findViewById<AppCompatButton>(R.id.fragment_btn_2).setOnClickListener(this)
        view.findViewById<AppCompatButton>(R.id.fragment_btn_3).setOnClickListener(this)
        val adapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_spinner_item,
            listOf("Spinner Item 1", "Spinner Item 2", "Spinner Item 3", "Spinner Item 4")
        )
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        view.findViewById<AppCompatSpinner>(R.id.fragment_spinner).adapter = adapter
        val searchView = view.findViewById<SearchView>(R.id.fragment_searchview)
        searchView.setSearchableInfo(
            (context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(
                ComponentName(context!!, MainActivity::class.java)
            )
        )
        searchView.seslSetUpButtonVisibility(View.VISIBLE)
        searchView.seslSetOnUpButtonClickListener(this)
    }

    override val layoutResId: Int = R.layout.fragment_widgets
    override val iconResId: Int = dev.oneuiproject.oneui.R.drawable.ic_oui_game_launcher
    override val title: CharSequence = "Widgets"
    override fun onClick(v: View) {
        // no-op
    }
}