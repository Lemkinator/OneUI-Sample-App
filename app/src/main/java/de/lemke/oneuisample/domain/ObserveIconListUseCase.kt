package de.lemke.oneuisample.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.util.IconAdapter.Icon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveIconListUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userSettingsRepository: UserSettingsRepository,
) {
    val iconsId = dev.oneuiproject.oneui.R.drawable::class.java.declaredFields.map { field ->
        field.getInt(null).let { Icon(it, context.resources.getResourceEntryName(it)) }
    }

    operator fun invoke(): Flow<Pair<List<Icon>, String?>> = userSettingsRepository.observeUserSettings().map {
        if (!it.searchActive || it.search.isBlank()) {
            iconsId to null
        } else {
            iconsId.filter { icon -> icon.containsKeywords(it.search.trim().split(" ").toSet()) } to it.search
        }
    }
}