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
import androidx.appcompat.app.AppCompatActivity
import de.lemke.oneuisample.R
import de.lemke.oneuisample.data.UserSettingsRepository

private const val TAG = "AppStart"

/** Result category of an app launch relative to the previously recorded version. */
enum class AppStartResult {
    /** No previous installation was recorded (version code -1). */
    FIRST_TIME,

    /** The app was upgraded since the last launch (current version code > last version code). */
    FIRST_TIME_VERSION,

    /** Version code unchanged since last launch, or a downgrade (treated as normal). */
    NORMAL,
}

/**
 * Snapshot of version and TOS state captured at app launch.
 *
 * @property result The categorized outcome of this launch.
 * @property versionCode The current app version code.
 * @property versionName The current human-readable version string.
 * @property lastVersionCode The version code recorded on the previous launch, or -1 if never set.
 * @property lastVersionName The version name recorded on the previous launch.
 * @property tosVersion The TOS version required by the current build.
 * @property acceptedTosVersion The highest TOS version the user has accepted, or -1 if never set.
 */
class AppStart(
    val result: AppStartResult,
    val versionCode: Int,
    val versionName: String,
    val lastVersionCode: Int,
    val lastVersionName: String,
    val tosVersion: Int,
    val acceptedTosVersion: Int,
) {
    /** `true` if no previous installation was recorded. */
    val isFirstTime get() = lastVersionCode == -1

    /** `true` if the app was upgraded since the last launch. */
    val isFirstTimeVersion get() = lastVersionCode in 0..<versionCode

    /** `true` if the user has accepted the current TOS version. */
    val tosAccepted get() = acceptedTosVersion >= tosVersion

    /** `true` if OOBE should be shown (first install or TOS not accepted). */
    val shouldShowOOBE get() = isFirstTime || !tosAccepted

    override fun toString(): String =
        "AppStart(result=$result, versionCode=$versionCode, versionName='$versionName', " +
            "lastVersionCode=$lastVersionCode, lastVersionName='$lastVersionName', " +
            "tosVersion=$tosVersion, acceptedTosVersion=$acceptedTosVersion)"

    /** `true` if [threshold] falls within the range of version codes updated across on this launch. */
    fun versionThresholdPassed(threshold: Int) = lastVersionCode >= 0 && threshold in lastVersionCode..<versionCode
}

/** Checks whether this is the first run, a version upgrade, or a normal start. Version info is committed by the caller. */
internal fun AppCompatActivity.checkAppStart(
    userSettings: UserSettingsRepository,
    versionCode: Int,
    versionName: String,
): AppStart {
    val lastVersionCode = userSettings.lastVersionCode
    val lastVersionName = userSettings.lastVersionName
    val tosVersion = resources.getInteger(R.integer.tos_version)
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
