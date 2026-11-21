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

import android.os.SystemClock
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.navigation.NavigationView
import de.lemke.oneuisample.ui.AboutActivity
import dev.oneuiproject.oneui.layout.NavDrawerLayout
import dev.oneuiproject.oneui.navigation.widget.DrawerNavigationView
import dev.oneuiproject.oneui.R as iconsR

private const val NAV_RAIL_MIN_SIDE_MARGIN_DP = 14

/** Sets up the drawer header button with an info icon navigating to [AboutActivity] and configures the nav rail. */
fun NavDrawerLayout.setupHeaderAndNavRail(aboutApp: String) {
    setHeaderButtonIcon(AppCompatResources.getDrawable(context, iconsR.drawable.ic_oui_info_outline))
    setHeaderButtonTooltip(aboutApp)
    setHeaderButtonOnClickListener { it.popOverOrStartActivity(AboutActivity::class.java) }
    setNavRailContentMinSideMargin(NAV_RAIL_MIN_SIDE_MARGIN_DP)
    closeNavRailOnBack = true
}

/** Wraps [listener] to ignore repeated clicks within [interval] milliseconds, preventing double-navigation. */
fun DrawerNavigationView.onNavigationSingleClick(
    interval: Long = 600,
    listener: NavigationView.OnNavigationItemSelectedListener,
) {
    var lastClick = 0L
    setNavigationItemSelectedListener { item ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClick < interval) return@setNavigationItemSelectedListener false
        lastClick = currentTime
        listener.onNavigationItemSelected(item)
    }
}
