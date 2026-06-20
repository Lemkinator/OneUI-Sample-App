/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui.util

import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import de.lemke.oneuisample.data.UserSettingsRepository
import dev.oneuiproject.oneui.ktx.hideSoftInput
import dev.oneuiproject.oneui.layout.ToolbarLayout

fun Fragment.getSearchListener(
    userSettings: UserSettingsRepository,
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
