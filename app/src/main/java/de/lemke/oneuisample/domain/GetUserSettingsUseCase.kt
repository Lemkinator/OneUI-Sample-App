package de.lemke.oneuisample.domain

import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetUserSettingsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke() =
        withContext(Dispatchers.Default) {
            userSettingsRepository.getUserSettings()
        }
}
