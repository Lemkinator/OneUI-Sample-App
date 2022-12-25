package dev.oneuiproject.oneui.oneuisampleapp.domain

import dev.oneuiproject.oneui.oneuisampleapp.data.UserSettingsRepository
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