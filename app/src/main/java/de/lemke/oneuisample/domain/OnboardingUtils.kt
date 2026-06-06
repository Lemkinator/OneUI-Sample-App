package de.lemke.oneuisample.domain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import de.lemke.oneuisample.data.userSettings
import de.lemke.oneuisample.ui.OOBEActivity

const val EXTRA_SKIP_ONBOARDING = "skipOnboarding"
const val EXTRA_VERSION_CODE = "versionCode"
const val EXTRA_VERSION_NAME = "versionName"

fun AppCompatActivity.onboardIfNeeded(
    versionCode: Int,
    versionName: String,
    allowSkip: Boolean = false,
): Boolean {
    val appStart = checkAppStart(versionCode, versionName)
    val skipRequested = allowSkip && intent.getBooleanExtra(EXTRA_SKIP_ONBOARDING, false)
    if (!skipRequested && appStart.shouldShowOOBE) {
        startActivity(
            Intent(this, OOBEActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(EXTRA_VERSION_CODE, versionCode)
                .putExtra(EXTRA_VERSION_NAME, versionName),
        )
        finishWithFade()
        return false
    }
    userSettings.lastVersionCode = versionCode
    userSettings.lastVersionName = versionName
    return true
}
