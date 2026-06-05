package de.lemke.oneuisample.ui.util

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import de.lemke.oneuisample.domain.AppStart
import de.lemke.oneuisample.domain.CheckAppStartUseCase
import de.lemke.oneuisample.domain.GetUserSettingsUseCase
import de.lemke.oneuisample.ui.OOBEActivity

const val EXTRA_SKIP_ONBOARDING = "skipOnboarding"

suspend fun AppCompatActivity.onboardIfNeeded(
    checkAppStart: CheckAppStartUseCase,
    getUserSettings: GetUserSettingsUseCase,
    allowSkip: Boolean = false,
): Boolean {
    if (allowSkip && intent.getBooleanExtra(EXTRA_SKIP_ONBOARDING, false)) return true
    val shouldShowOOBE =
        when (checkAppStart()) {
            AppStart.FIRST_TIME -> true
            AppStart.NORMAL, AppStart.FIRST_TIME_VERSION -> !getUserSettings().tosAccepted
        }
    if (shouldShowOOBE) {
        startActivity(
            Intent(this, OOBEActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
        finishWithFade()
        return false
    }
    return true
}
