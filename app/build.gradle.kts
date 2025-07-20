import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.android.gms.oss-licenses-plugin")
}

fun String.toEnvVarStyle(): String = replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()
fun getProperty(key: String): String? = rootProject.findProperty(key)?.toString() ?: System.getenv(key.toEnvVarStyle())

android {
    namespace = "de.lemke.oneuisample"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.lemke.oneuisample"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
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
        all {
            signingConfig = signingConfigs.getByName(if (getProperty("releaseStoreFile").isNullOrEmpty()) "debug" else "release")
        }

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
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "OneUI Sample App (Debug)")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_21) } }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    //SESL Android Jetpack
    implementation("sesl.androidx.core:core:1.17.0-beta01+1.0.7-sesl8+rev0")
    implementation("sesl.androidx.core:core-ktx:1.17.0-beta01+1.0.0-sesl8+rev0")
    implementation("sesl.androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01+1.0.0-sesl8+rev0")
    implementation("sesl.androidx.fragment:fragment:1.8.8+1.0.5-sesl8+rev0")
    implementation("sesl.androidx.appcompat:appcompat:1.7.1+1.0.18-sesl8+rev0")
    implementation("sesl.androidx.recyclerview:recyclerview:1.4.0+1.0.12-sesl8+rev0")
    implementation("sesl.androidx.picker:picker-basic:1.0.7+1.0.7-sesl8+rev0")
    implementation("sesl.androidx.picker:picker-app:1.0.6+1.0.6-sesl8+rev0")
    implementation("sesl.androidx.indexscroll:indexscroll:1.0.0+1.0.0-sesl8+rev0")
    implementation("sesl.androidx.preference:preference:1.2.1+1.0.0-sesl8+rev0")
    implementation("sesl.androidx.viewpager2:viewpager2:1.1.0+1.0.0-sesl8+rev0")
    implementation("sesl.androidx.picker:picker-color:1.0.2+1.0.2-sesl8+rev0")
    //SESL Material Components + Design Lib + Icons
    implementation("sesl.com.google.android.material:material:1.12.0+1.0.31-sesl8+rev1")
    implementation("io.github.tribalfs:oneui-design:0.7.6+oneui7")
    implementation("io.github.oneuiproject:icons:1.1.0")

    implementation("com.airbnb.android:lottie:6.6.7")
    implementation("com.google.android.gms:play-services-oss-licenses:17.2.1")
    implementation("androidx.core:core-splashscreen:1.2.0-rc01")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    val navVersion = "2.9.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    implementation("com.google.dagger:hilt-android:2.57")
    ksp("com.google.dagger:hilt-compiler:2.57")
}

configurations.implementation {
    //Exclude official android jetpack modules
    exclude ("androidx.core", "core")
    exclude ("androidx.core", "core-ktx")
    exclude ("androidx.customview", "customview")
    exclude ("androidx.coordinatorlayout", "coordinatorlayout")
    exclude ("androidx.drawerlayout", "drawerlayout")
    exclude ("androidx.viewpager2", "viewpager2")
    exclude ("androidx.viewpager", "viewpager")
    exclude ("androidx.appcompat", "appcompat")
    exclude ("androidx.fragment", "fragment")
    exclude ("androidx.preference", "preference")
    exclude ("androidx.recyclerview", "recyclerview")
    exclude ("androidx.slidingpanelayout", "slidingpanelayout")
    exclude ("androidx.swiperefreshlayout", "swiperefreshlayout")

    //Exclude official material components lib
    exclude ("com.google.android.material", "material")
}
