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

@file:OptIn(ExperimentalRoborazziApi::class)

import com.android.build.api.variant.Component
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import dev.detekt.gradle.Detekt
import java.io.File
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.kover)
    alias(libs.plugins.android.junit)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
}

fun String.toEnvVarStyle(): String = replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()

fun getProperty(key: String): String? = rootProject.findProperty(key)?.toString() ?: System.getenv(key.toEnvVarStyle())

android {
    namespace = "de.lemke.oneuisample"
    compileSdk {
        version =
            release(
                libs.versions.compileSdk
                    .get()
                    .toInt(),
            ) {
                minorApiLevel =
                    libs.versions.compileSdkMinor
                        .get()
                        .toInt()
            }
    }
    defaultConfig {
        applicationId = "de.lemke.oneuisample"
        minSdk = 26
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 2
        versionName = "1.0.1"
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

    @Suppress("UnstableApiUsage")
    testFixtures {
        enable = true
    }

    packaging {
        resources {
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/licenses/**"
        }
    }
    lint {
        warningsAsErrors = true
        checkDependencies = true
        // Explicit: pins intent against future AGP default changes.
        checkReleaseBuilds = true
        abortOnError = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true

            all { test ->
                test.useJUnitPlatform()
                test.maxHeapSize = "4096m"
                test.jvmArgs(
                    "-XX:+EnableDynamicAgentLoading",
                    "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "--add-opens=java.base/java.util=ALL-UNNAMED",
                    "--add-opens=java.base/java.io=ALL-UNNAMED",
                    "--add-opens=java.base/java.net=ALL-UNNAMED",
                    "--add-opens=java.base/java.security=ALL-UNNAMED",
                    "--add-opens=java.base/java.text=ALL-UNNAMED",
                    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
                    "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
                )
                test.testLogging.exceptionFormat = TestExceptionFormat.FULL
                test.systemProperty("robolectric.graphicsMode", "NATIVE")
                test.systemProperty("roborazzi.test.record", project.findProperty("roborazzi.record") ?: "false")
                test.systemProperty("roborazzi.test.verify", project.findProperty("roborazzi.verify") ?: "true")
            }
        }
        animationsDisabled = true
    }
}
androidComponents {
    listOf("nonMinifiedRelease", "benchmarkRelease").forEach { buildType ->
        onVariants(selector().withBuildType(buildType)) { variant ->
            variant.buildConfigFields!!.put(
                "FIRST_RUN_SKIPPABLE",
                com.android.build.api.variant
                    .BuildConfigField("boolean", "true", "Allow benchmarks to skip the first-run chain"),
            )
        }
    }
}

dependencies {
    implementation(libs.bundles.oneui)
    implementation(libs.lottie)
    implementation(libs.aboutlibraries.compose.core)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.aboutlibraries.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.core.splashscreen)
    implementation(libs.coroutines.core)
    runtimeOnly(libs.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    runtimeOnly(libs.profileinstaller)
    baselineProfile(project(":benchmarks"))
    debugImplementation(libs.leakcanary)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.bundles.robolectric.test)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.konsist)
    testImplementation(libs.junit4)
    testImplementation(libs.roborazzi.core)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
    kspTest(libs.hilt.compiler)

    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    testFixturesImplementation(libs.androidx.test.core)
    testFixturesImplementation(libs.androidx.material3)
    testFixturesImplementation(libs.coroutines.test)
    // PreferenceXmlParity.kt hosts a real PreferenceFragmentCompat (via oneui.design's androidx.preference
    // fork) through Robolectric.buildActivity - `implementation` deps of :app's main source set don't leak
    // to testFixtures, so both are declared again here at the same coordinates main uses.
    testFixturesImplementation(libs.oneui.design)
    testFixturesImplementation(libs.robolectric)
    testFixturesImplementation(libs.hilt.android.testing)
    kspTestFixtures(libs.hilt.compiler)
}

spotless {
    kotlin {
        target("src/**/*.kt")
        licenseHeaderFile(rootProject.file("config/spotless/apache-2.0.kt"))
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("xml") {
        target("src/**/*.xml")
        licenseHeaderFile(rootProject.file("config/spotless/apache-2.0.xml"), "(<[^!?])")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom(layout.projectDirectory.dir("src"))
    buildUponDefaultConfig = true
    parallel = true
    autoCorrect = false
    ignoredBuildTypes = listOf("nonMinifiedRelease", "benchmarkRelease")
}

tasks.named("check") {
    setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name == "detekt" })
    dependsOn("detektMain", "detektTest")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = libs.versions.jvmTarget.get()
    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}

androidComponents {
    val referenceBuildType = "debug"
    val referenceSourceDirs = objects.listProperty<File>()
    onVariants { variant ->
        val components = listOf(variant) + variant.nestedComponents
        if (variant.buildType == referenceBuildType) components.forEach { referenceSourceDirs.addAll(it.sourceDirs()) }
        components.forEach { component ->
            tasks
                .withType<Detekt>()
                .named { it == "detekt${component.name.replaceFirstChar(Char::uppercase)}" }
                .configureEach {
                    val kotlinc = tasks.named<KotlinCompile>(component.computeTaskName("compile", "Kotlin"))
                    val javac = tasks.named<JavaCompile>(component.computeTaskName("compile", "JavaWithJavac"))
                    // from() discards the plugin's default classpath (compile libraries and classes dirs), so both are added back.
                    classpath.from(
                        kotlinc.map { it.libraries },
                        kotlinc.flatMap { it.destinationDirectory },
                        javac.flatMap { it.destinationDirectory },
                    )
                    if (variant.buildType != referenceBuildType) {
                        exclude { element -> referenceSourceDirs.get().any { element.file.startsWith(it) } }
                    }
                }
        }
    }
}

fun Component.sourceDirs(): Provider<Set<File>> =
    listOfNotNull(sources.java, sources.kotlin)
        .map { it.static.map { dirs -> dirs.map(Directory::getAsFile).toSet() } }
        .reduce { acc, dirs -> acc.zip(dirs) { a, b -> a + b } }

baselineProfile {
    // Generation is wired to the release variant only, so a debug or PR assemble never boots the GMD.
    automaticGenerationDuringBuild = false
    dexLayoutOptimization = true
    saveInSrc = false
    variants {
        create("release") {
            automaticGenerationDuringBuild = true
        }
    }
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
                    "*_Impl",
                    "*_Impl\$*",
                    "*ComposableSingletons\$*",
                )
                annotatedBy("de.lemke.oneuisample.NoCoverage")
            }
        }
        variant("debug") {
            verify {
                rule {
                    minBound(100, coverageUnits = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.INSTRUCTION)
                    minBound(100, coverageUnits = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH)
                }
            }
        }
    }
}
