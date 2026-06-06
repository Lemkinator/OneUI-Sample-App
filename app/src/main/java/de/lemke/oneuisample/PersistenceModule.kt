package de.lemke.oneuisample

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    @Provides
    @Singleton
    fun provideUserSettingsRepository(
        @ApplicationContext context: Context,
    ): UserSettingsRepository = UserSettingsRepository(context.getSharedPreferences("user_settings", Context.MODE_PRIVATE))
}
