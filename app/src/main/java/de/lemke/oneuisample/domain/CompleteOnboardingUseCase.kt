/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
class CompleteOnboardingUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userSettings: UserSettingsRepository,
) {
    suspend operator fun invoke(
        versionCode: Int,
        versionName: String,
    ) = withContext(Dispatchers.Default) {
        userSettings.update {
            copy(
                acceptedTosVersion = context.resources.getInteger(R.integer.tos_version),
                lastVersionCode = versionCode,
                lastVersionName = versionName,
            )
        }
    }
}
