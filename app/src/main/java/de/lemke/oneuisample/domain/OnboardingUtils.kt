package de.lemke.oneuisample.domain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import de.lemke.oneuisample.data.UserSettingsRepository
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
