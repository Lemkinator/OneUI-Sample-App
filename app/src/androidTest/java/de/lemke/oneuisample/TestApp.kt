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
package de.lemke.oneuisample

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.testing.CustomTestApplication
import de.lemke.oneuisample.data.UserSettings

open class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        // Bypass OOBE: fresh SharedPreferences has lastVersionCode = -1 which triggers
        // onboardIfNeeded → finishWithFade, leaving MainActivity DESTROYED.
        getSharedPreferences(UserSettings.PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(UserSettings::lastVersionCode.name, Int.MAX_VALUE)
            .putInt(UserSettings::acceptedTosVersion.name, Int.MAX_VALUE)
            .apply()
    }
}

@Suppress("unused") // KSP generates TestApplication_Application from this annotation target
@CustomTestApplication(TestApp::class)
interface TestApplication
