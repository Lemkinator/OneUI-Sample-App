package de.lemke.oneuisample

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.data.UserSettingsRepository.Companion.PREFS_NAME
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun providesApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideUserSettingsRepository(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): UserSettingsRepository = UserSettingsRepository(context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE), scope)
}
