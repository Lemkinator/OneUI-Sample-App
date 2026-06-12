plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "de.lemke.oneuisample.benchmarks"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()
    defaultConfig {
        minSdk = 28
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
    testOptions.managedDevices.localDevices {
        @Suppress("UnstableApiUsage")
        create("pixel9Api35") {
            device = "Pixel 9"
            apiLevel = 35
            systemImageSource = "aosp"
            testedAbi = "x86_64"
        }
    }
}

dependencies {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.uiautomator)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
}

baselineProfile {
    @Suppress("UnstableApiUsage")
    enableEmulatorDisplay = false
    managedDevices.clear()
    managedDevices += "pixel9Api35"
    useConnectedDevices = false
}
