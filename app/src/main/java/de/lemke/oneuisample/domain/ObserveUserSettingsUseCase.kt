package de.lemke.oneuisample.domain

import de.lemke.oneuisample.data.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ObserveUserSettingsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    operator fun invoke() = userSettingsRepository.observeUserSettings().flowOn(Dispatchers.Default)
}