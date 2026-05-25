package de.lemke.oneuisample.domain

import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class ObserveUserSettingsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    operator fun invoke() = userSettingsRepository.observeUserSettings().flowOn(Dispatchers.Default)
}
