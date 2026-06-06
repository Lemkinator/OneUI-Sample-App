package de.lemke.oneuisample.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.oneuisample.data.search
import de.lemke.oneuisample.data.searchActive
import de.lemke.oneuisample.data.userSettings
import de.lemke.oneuisample.ui.util.IconAdapter.Icon
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class ObserveIconListUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val iconsId =
        dev.oneuiproject.oneui.R.drawable::class.java.declaredFields
            .filter { it.type == Int::class.java }
            .mapNotNull { field ->
                runCatching {
                    field.isAccessible = true
                    field.getInt(null).let { Icon(it, context.resources.getResourceEntryName(it)) }
                }.getOrNull()
            }

    operator fun invoke() =
        combine(userSettings.flow.search, userSettings.flow.searchActive) { search, active ->
            if (!active || search.isBlank()) {
                iconsId to null
            } else {
                val keywords = search.trim().split(" ").toSet()
                iconsId.filter { it.containsKeywords(keywords) } to search
            }
        }.distinctUntilChanged()
}
