package de.lemke.oneuisample.domain

import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import de.lemke.oneuisample.data.userSettings
import dev.oneuiproject.oneui.ktx.hideSoftInput
import dev.oneuiproject.oneui.layout.ToolbarLayout

fun Fragment.getSearchListener(
    @StringRes queryHint: Int? = null,
    onActivate: SearchView.() -> Unit = {},
): ToolbarLayout.SearchModeListener =
    object : ToolbarLayout.SearchModeListener {
        override fun onQueryTextSubmit(query: String?): Boolean = setSearch(query).also { requireActivity().hideSoftInput() }

        override fun onQueryTextChange(query: String?): Boolean = setSearch(query)

        private fun setSearch(query: String?): Boolean {
            if (!userSettings.searchActive) return false
            userSettings.search = query ?: ""
            return true
        }

        override fun onSearchModeToggle(
            searchView: SearchView,
            isActive: Boolean,
        ) {
            userSettings.searchActive = isActive
            if (isActive) {
                queryHint?.let { searchView.queryHint = getString(it) }
                searchView.setQuery(userSettings.search, false)
                onActivate(searchView)
            }
        }
    }
