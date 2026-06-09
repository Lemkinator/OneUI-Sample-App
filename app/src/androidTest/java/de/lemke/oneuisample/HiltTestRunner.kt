package de.lemke.oneuisample

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

@Suppress("unused") // referenced by string in testInstrumentationRunner build config
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        name: String,
        ctx: Context,
    ): Application = super.newApplication(cl, TestApplication_Application::class.java.name, ctx)
}
