package de.lemke.oneuisample.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AcceptTosUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userSettings: UserSettingsRepository,
) {
    suspend operator fun invoke(
        versionCode: Int,
        versionName: String,
    ) = withContext(Dispatchers.Default) {
        userSettings.acceptedTosVersion = context.resources.getInteger(R.integer.tos_version)
        userSettings.lastVersionCode = versionCode
        userSettings.lastVersionName = versionName
    }
}
