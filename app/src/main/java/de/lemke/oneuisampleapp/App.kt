package de.lemke.oneuisampleapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main entry point into the application process.
 * Registered in the AndroidManifest.xml file.
 */
@HiltAndroidApp
class App : Application()

