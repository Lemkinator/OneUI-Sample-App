package de.lemke.oneuisampleapp.domain

import de.lemke.oneuisampleapp.data.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
        userSettingsRepository.getSettings()
    }
}