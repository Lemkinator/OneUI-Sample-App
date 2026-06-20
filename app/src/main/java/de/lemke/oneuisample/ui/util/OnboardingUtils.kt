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
package de.lemke.oneuisample.ui.util

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.domain.AppStart
import de.lemke.oneuisample.domain.checkAppStart
import de.lemke.oneuisample.ui.OOBEActivity

const val EXTRA_SKIP_ONBOARDING = "skipOnboarding"
const val EXTRA_VERSION_CODE = "versionCode"
const val EXTRA_VERSION_NAME = "versionName"

fun AppCompatActivity.onboardIfNeeded(
    userSettings: UserSettingsRepository,
    versionCode: Int,
    versionName: String,
    allowSkip: Boolean = false,
): AppStart? {
    val appStart = checkAppStart(userSettings, versionCode, versionName)
    if (!(allowSkip && intent.getBooleanExtra(EXTRA_SKIP_ONBOARDING, false)) && appStart.shouldShowOOBE) {
        startActivity(
            Intent(this, OOBEActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(EXTRA_VERSION_CODE, versionCode)
                .putExtra(EXTRA_VERSION_NAME, versionName),
        )
        finishWithFade()
        return null
    }
    userSettings.lastVersionCode = versionCode
    userSettings.lastVersionName = versionName
    overrideFadeOpenTransition()
    return appStart
}
