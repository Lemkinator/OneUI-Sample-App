package de.lemke.oneuisample.domain

import de.lemke.oneuisample.data.AppsRepository
import de.lemke.oneuisample.data.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAppsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val apps: AppsRepository,
) {
    operator fun invoke(): Flow<List<String>> = userSettingsRepository.observeShowSystemApps().map { showSystemApps ->
        if (showSystemApps) {
            apps.get()
        } else {
            apps.get().filterNot { it.isSystemApp }
        }.map { it.packageName }
    }.flowOn(Dispatchers.Default)
}

