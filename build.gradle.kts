import com.android.build.api.dsl.CommonExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.android.junit) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.dependency.analysis)
}

/**
 * Converts a camelCase or mixedCase string to ENV_VAR_STYLE (uppercase with underscores).
 * Example: githubAccessToken -> GITHUB_ACCESS_TOKEN
 */
fun String.toEnvVarStyle(): String = replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()

/**
 * Note: To configure GitHub credentials, you have to generate an access token with at least `read:packages` scope at
 * https://github.com/settings/tokens/new and then add it to any of the following:
 *
 * - Add `ghUsername` and `ghAccessToken` to Global Gradle Properties
 * - Set `GH_USERNAME` and `GH_ACCESS_TOKEN` in your environment variables or
 * - Create a `github.properties` file in your project folder with the following content:
 *      ghUsername=&lt;YOUR_GITHUB_USERNAME&gt;
 *      ghAccessToken=&lt;YOUR_GITHUB_ACCESS_TOKEN&gt;
 */
fun getProperty(key: String): String =
    Properties()
        .apply {
            rootProject
                .file("github.properties")
                .takeIf { it.exists() }
                ?.inputStream()
                ?.use { load(it) }
        }.getProperty(key)
        ?: rootProject.findProperty(key)?.toString()
        ?: System.getenv(key.toEnvVarStyle())
        ?: throw GradleException("Property $key not found")

val githubUsername = getProperty("ghUsername")
val githubAccessToken = getProperty("ghAccessToken")

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.pkg.github.com/tribalfs/oneui-design") {
            credentials {
                username = githubUsername
                password = githubAccessToken
            }
        }
    }
}

subprojects {
    plugins.withId("com.android.base") {
        project.extensions.findByType(CommonExtension::class.java)?.apply {
            compileOptions.apply {
                sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
                targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
            }

            @Suppress("UnstableApiUsage")
            testOptions.managedDevices.localDevices {
                register("pixel9Api35") {
                    device = "Pixel 9"
                    apiLevel = 35
                    systemImageSource = "aosp"
                    testedAbi = "x86_64" // preserve ABI selection before AGP 10.0 changes default to arm64-v8a
                }
            }

            // oneui-design replaces these AOSP AndroidX modules with Samsung forks; exclude
            // AOSP originals from all com.android.application modules to prevent shadowing.
            // com.android.test modules (e.g. :benchmarks) are not matched and keep
            // genuine AOSP AndroidX for UiAutomator and benchmark dependencies.
            plugins.withId("com.android.application") {
                // Exclude from production AND androidTest* configs (not unit-test* which need
                // real AOSP AndroidX for Robolectric).
                configurations.matching { !it.name.startsWith("test", ignoreCase = true) }.configureEach {
                    exclude(group = "androidx.core", module = "core")
                    exclude(group = "androidx.core", module = "core-ktx")
                    exclude(group = "androidx.customview", module = "customview")
                    exclude(group = "androidx.coordinatorlayout", module = "coordinatorlayout")
                    exclude(group = "androidx.drawerlayout", module = "drawerlayout")
                    exclude(group = "androidx.viewpager2", module = "viewpager2")
                    exclude(group = "androidx.viewpager", module = "viewpager")
                    exclude(group = "androidx.appcompat", module = "appcompat")
                    exclude(group = "androidx.fragment", module = "fragment")
                    exclude(group = "androidx.preference", module = "preference")
                    exclude(group = "androidx.recyclerview", module = "recyclerview")
                    exclude(group = "androidx.slidingpanelayout", module = "slidingpanelayout")
                    exclude(group = "androidx.swiperefreshlayout", module = "swiperefreshlayout")
                    exclude(group = "com.google.android.material", module = "material")
                }
            }
        }
    }
}
