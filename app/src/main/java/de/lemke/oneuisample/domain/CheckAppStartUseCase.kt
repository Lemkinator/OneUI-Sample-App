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

import android.util.Log
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.di.TosVersion
import javax.inject.Inject

private const val TAG = "AppStart"

/** Checks whether this is the first run, a version upgrade, or a normal start. The caller commits version info. */
class CheckAppStartUseCase @Inject constructor(
    private val userSettings: UserSettings,
    @param:TosVersion private val tosVersion: Int,
) {
    operator fun invoke(
        versionCode: Int,
        versionName: String,
    ): AppStart {
        val lastVersionCode = userSettings.lastVersionCode
        val lastVersionName = userSettings.lastVersionName
        val acceptedTosVersion = userSettings.acceptedTosVersion
        val result =
            when {
                lastVersionCode == -1 -> {
                    AppStartResult.FIRST_TIME
                }

                lastVersionCode < versionCode -> {
                    AppStartResult.FIRST_TIME_VERSION
                }

                lastVersionCode > versionCode -> {
                    Log.w(TAG, "Current version code ($versionCode) is less than the one recognized on last startup ($lastVersionCode). ")
                    Log.w(TAG, "Defensively assuming normal app start.")
                    AppStartResult.NORMAL
                }

                else -> {
                    AppStartResult.NORMAL
                }
            }
        return AppStart(result, versionCode, versionName, lastVersionCode, lastVersionName, tosVersion, acceptedTosVersion).apply {
            Log.d(TAG, this.toString())
        }
    }
}
