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

import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.di.DefaultDispatcher
import de.lemke.oneuisample.di.TosVersion
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class CompleteOnboardingUseCase @Inject constructor(
    @param:TosVersion private val tosVersion: Int,
    private val userSettings: UserSettings,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        versionCode: Int,
        versionName: String,
    ) = withContext(defaultDispatcher) {
        userSettings.acceptedTosVersion = tosVersion
        userSettings.lastVersionCode = versionCode
        userSettings.lastVersionName = versionName
    }
}
