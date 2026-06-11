@file:OptIn(ExperimentalRoborazziApi::class)

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.junit)
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.spotless)
}

fun String.toEnvVarStyle(): String = replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()

fun getProperty(key: String): String? = rootProject.findProperty(key)?.toString() ?: System.getenv(key.toEnvVarStyle())

android {
    namespace = "de.lemke.oneuisample"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()
    defaultConfig {
        applicationId = "de.lemke.oneuisample"
        minSdk = 26
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "de.lemke.oneuisample.HiltTestRunner"
        buildConfigField("boolean", "FIRST_RUN_SKIPPABLE", "false")
    }
    @Suppress("UnstableApiUsage")
    androidResources.localeFilters += listOf("en", "de")
    signingConfigs {
        create("release") {
            getProperty("releaseStoreFile").apply {
                if (isNullOrEmpty()) {
                    logger.warn("Release signing configuration not found. Using debug signing config.")
                } else {
                    logger.lifecycle("Using release signing configuration from: $this")
                    storeFile = rootProject.file(this)
                    storePassword = getProperty("releaseStorePassword")
                    keyAlias = getProperty("releaseKeyAlias")
                    keyPassword = getProperty("releaseKeyPassword")
                }
            }
        }
    }
    buildTypes {
        all { signingConfig = signingConfigs.getByName(if (getProperty("releaseStoreFile").isNullOrEmpty()) "debug" else "release") }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk { debugSymbolLevel = "FULL" }
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            //noinspection NotShrinkingResources
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/licenses/**"
        }
        jniLibs.useLegacyPackaging = true // sets extractNativeLibs=true; affects only APK install-time .so extraction, not AAB publishing
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                test.useJUnitPlatform()
                test.jvmArgs("-XX:+EnableDynamicAgentLoading")
                test.systemProperty("robolectric.graphicsMode", "NATIVE")
            }
        }
        animationsDisabled = true
    }
    lint {
        warningsAsErrors = true
        // checkDependencies = false: private AAR deps surface
        // hundreds of unactionable warnings; flip to true once in-project surface is clean
        checkDependencies = false
        checkReleaseBuilds = true
        abortOnError = true
        baseline = file("lint-baseline.xml")
        sarifReport = true
        htmlReport = true
        // Avatar PNGs in drawable/ are intentionally densityless (photos, not icons)
        disable += setOf("IconLocation", "IconMissingDensityFolder")
    }
}
spotless {
    kotlin {
        target("src/**/*.kt")
        targetExclude("**/build/**", "**/generated/**")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
    format("xml") {
        target("src/**/*.xml")
        targetExclude("**/build/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    autoCorrect = false
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    jvmTarget = libs.versions.jvmTarget.get()
    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}

dependencies {
    implementation(libs.bundles.oneui)
    implementation(libs.lottie)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.core.splashscreen)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    debugImplementation(libs.leakcanary)

    testImplementation(libs.arch.core.testing)
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.bundles.robolectric.test)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.konsist)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
    kspTest(libs.hilt.compiler)

    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
    compare {
        outputDir.set(layout.buildDirectory.dir("reports/roborazzi"))
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.databinding.*",
                    "*.BuildConfig",
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*_Factory",
                    "*_Provide*",
                    "*_MembersInjector",
                    "dagger.hilt.*",
                    "hilt_aggregated_deps.*",
                    "*.di.*",
                    "*Activity",
                    "*Activity$*",
                    "*Fragment",
                    "*Fragment$*",
                    "*Adapter",
                    "*Adapter$*",
                    "*Receiver",
                    "*Receiver$*",
                    "*TileService",
                    "*TileService$*",
                    "*DebugTools*",
                    "*ComposableSingletons$*",
                )
            }
        }
        variant("debug") {
            verify {
                rule { minBound(95) }
            }
        }
    }
}
