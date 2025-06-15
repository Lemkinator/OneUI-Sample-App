@file:Suppress("unused")

package de.lemke.oneuisample.ui.util

import android.content.Intent
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.navigation.NavigationView
import de.lemke.oneuisample.ui.AboutActivity
import dev.oneuiproject.oneui.layout.NavDrawerLayout
import dev.oneuiproject.oneui.navigation.widget.DrawerNavigationView
import dev.oneuiproject.oneui.R as iconsR

private const val TAG = "DrawerUtils"

fun NavDrawerLayout.setupHeaderAndNavRail(aboutApp: String) {
    setupHeaderButton(
        icon = AppCompatResources.getDrawable(context, iconsR.drawable.ic_oui_info_outline)!!,
        tooltipText = aboutApp,
        listener = { context.startActivity(Intent(context, AboutActivity::class.java)) }
    )
    setNavRailContentMinSideMargin(14)
    closeNavRailOnBack = true
}

private var lastClick = 0L

fun DrawerNavigationView.onNavigationSingleClick(interval: Long = 600, listener: NavigationView.OnNavigationItemSelectedListener) {
    setNavigationItemSelectedListener { item ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClick < interval) return@setNavigationItemSelectedListener false
        lastClick = currentTime
        listener.onNavigationItemSelected(item)
    }
}
