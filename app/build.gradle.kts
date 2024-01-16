plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    //id("com.google.devtools.ksp")
}

val releaseStoreFile: String? by rootProject
val releaseStorePassword: String? by rootProject
val releaseKeyAlias: String? by rootProject
val releaseKeyPassword: String? by rootProject

android {
    namespace = "dev.oneuiproject.oneui.oneuisampleapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.oneuiproject.oneui.oneuisampleapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        resourceConfigurations += listOf("en", "de")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            releaseStoreFile?.also {
                storeFile = rootProject.file(it)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        all {
            signingConfig =
                if (releaseStoreFile.isNullOrEmpty()) {
                    signingConfigs.getByName("debug")
                } else {
                    signingConfigs.getByName("release")
                }
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

configurations.configureEach {
    exclude("androidx.appcompat", "appcompat")
    exclude("androidx.core",  "core")
    exclude("androidx.core",  "core-ktx")
    exclude("androidx.customview",  "customview")
    exclude("androidx.viewpager",  "viewpager")
    exclude("androidx.drawerlayout",  "drawerlayout")
    exclude("androidx.viewpager",  "viewpager")
    exclude("androidx.viewpager2",  "viewpager2")
    exclude("androidx.coordinatorlayout",  "coordinatorlayout")
    exclude("androidx.recyclerview",  "recyclerview")
    exclude("io.github.oneuiproject.sesl", "fragment")
}

dependencies {
    implementation("io.github.oneuiproject:design:1.2.6")

    implementation("io.github.oneuiproject.sesl:appcompat:1.4.0")
    implementation("io.github.oneuiproject.sesl:preference:1.1.0")
    implementation("io.github.oneuiproject.sesl:recyclerview:1.4.1")
    implementation("io.github.oneuiproject.sesl:swiperefreshlayout:1.0.0")
    implementation("io.github.oneuiproject.sesl:viewpager:1.1.0")
    implementation("io.github.oneuiproject.sesl:viewpager2:1.1.0")

    implementation("io.github.oneuiproject.sesl:material:1.5.0")

    implementation("io.github.oneuiproject.sesl:apppickerview:1.0.0")
    implementation("io.github.oneuiproject.sesl:indexscroll:1.0.3")
    implementation("io.github.oneuiproject.sesl:picker-basic:1.2.0")
    implementation("io.github.oneuiproject.sesl:picker-color:1.1.0")

    implementation("io.github.oneuiproject:icons:1.1.0")

    implementation("io.github.oneuiproject.sesl:core:2.0.0-beta01") //sesl5 mavenLocal
    implementation("io.github.oneuiproject.sesl:core-ktx:2.0.0-beta01") //sesl5 mavenLocal
    implementation("io.github.oneuiproject.sesl:coordinatorlayout:2.0.0") //sesl5 mavenLocal
    implementation("io.github.oneuiproject.sesl:drawerlayout:2.0.0") //sesl5 mavenLocal
    implementation("io.github.oneuiproject.sesl:customview:2.0.0") //sesl5 mavenLocal

    implementation ("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("androidx.core:core-splashscreen:1.1.0-alpha02")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
}