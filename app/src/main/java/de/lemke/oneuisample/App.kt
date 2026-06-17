/*
 * Copyright 2024-2026 Leonard Lemke
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
package de.lemke.oneuisample

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import dagger.hilt.android.HiltAndroidApp
import de.lemke.oneuisample.data.UserSettingsRepository
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var userSettings: UserSettingsRepository

    override fun onCreate() {
        super.onCreate()
        when {
            userSettings.autoDarkMode -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            userSettings.darkMode -> setDefaultNightMode(MODE_NIGHT_YES)
            else -> setDefaultNightMode(MODE_NIGHT_NO)
        }
    }
}
