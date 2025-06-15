package de.lemke.oneuisample.data

import android.content.Context
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.oneuisample.ui.util.toast
import de.lemke.oneuisample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.also
import kotlin.collections.map

class AppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    data class App(
        val packageName: String,
        val label: String,
        val isSystemApp: Boolean,
    )

    private fun PackageManager.getInstalledPackagesCompat(flags: Int = 0): List<PackageInfo> =
        if (SDK_INT >= TIRAMISU) getInstalledPackages(PackageInfoFlags.of(flags.toLong())) else getInstalledPackages(0)

    private var apps: List<App>? = null

    suspend fun get() = apps ?: try {
        withContext(Dispatchers.IO) {
            context.packageManager.getInstalledPackagesCompat(GET_META_DATA).map {
                App(
                    it.packageName,
                    (it.applicationInfo?.loadLabel(context.packageManager) ?: "").toString(),
                    (it.applicationInfo?.flags?.and(FLAG_SYSTEM) ?: 1) != 0
                )
            }.also { apps = it }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) { context.toast(R.string.error_loading_apps) }
        emptyList<App>()
    }
}

