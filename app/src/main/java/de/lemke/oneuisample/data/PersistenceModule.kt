package de.lemke.oneuisample.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    @Provides
    @Singleton
    fun provideUserSettingsRepository(
        @ApplicationContext context: Context,
    ): UserSettingsRepository =
        UserSettingsRepository(context.getSharedPreferences("user_settings", MODE_PRIVATE))
}
