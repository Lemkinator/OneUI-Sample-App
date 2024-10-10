plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

val releaseStoreFile: String? by rootProject
val releaseStorePassword: String? by rootProject
val releaseKeyAlias: String? by rootProject
val releaseKeyPassword: String? by rootProject

android {
    namespace = "dev.oneuiproject.oneui.oneuisampleapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.oneuiproject.oneui.oneuisampleapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        resourceConfigurations += listOf("en", "de")
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
            signingConfig = signingConfigs.getByName(if (releaseStoreFile.isNullOrEmpty()) "debug" else "release")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

dependencies {
    //sesl6 modules
    implementation("sesl.androidx.core:core:1.13.1+1.0.11-sesl6+rev1")
    implementation("sesl.androidx.core:core-ktx:1.13.1+1.0.0-sesl6+rev0")
    implementation("sesl.androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01+1.0.0-sesl6+rev0")
    implementation("sesl.androidx.fragment:fragment:1.8.3+1.0.0-sesl6+rev0")
    implementation("sesl.androidx.recyclerview:recyclerview:1.4.0-rc01+1.0.21-sesl6+rev0")
    implementation("sesl.androidx.appcompat:appcompat:1.7.0+1.0.34-sesl6+rev4")
    implementation("sesl.androidx.viewpager2:viewpager2:1.1.0+1.0.0-sesl6+rev0")
    implementation("sesl.androidx.preference:preference:1.2.1+1.0.4-sesl6+rev3")
    implementation("sesl.androidx.indexscroll:indexscroll:1.0.3+1.0.3-sesl6+rev2")
    implementation("sesl.androidx.picker:picker-basic:1.0.17+1.0.17-sesl6+rev2")
    implementation("sesl.androidx.picker:picker-color:1.0.6+1.0.6-sesl6+rev3")
    implementation("sesl.androidx.apppickerview:apppickerview:1.0.1+1.0.1-sesl6+rev2")
    implementation("sesl.com.google.android.material:material:1.12.0+1.0.23-sesl6+rev1")

    implementation(project(":lib"))
    implementation("io.github.oneuiproject:icons:1.1.0")

    implementation("com.airbnb.android:lottie:6.5.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
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
