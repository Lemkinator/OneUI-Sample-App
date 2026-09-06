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

val checkDependencyUpdates = providers.gradleProperty("lint.checkDependencyUpdates").getOrElse("true").toBoolean()

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

            // Renovate owns dependency freshness on its own PRs; enforcing there would fail every
            // in-flight bump against every other still-pending one.
            if (!checkDependencyUpdates) {
                lint.informational += setOf("GradleDependency", "NewerVersionAvailable")
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
                // Exclude from non-unit-test configs. Unit-test* configs need genuine AOSP
                // AndroidX for Robolectric. androidTest* configs keep SESL transitively from
                // :app implementation deps — instrumented tests launch SESL activities that call
                // SESL-specific APIs (e.g. MenuItemCompat.setSeslNaviMenuItemType).
                // contains("test") would put AOSP in androidTest and cause NoSuchMethodError.
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
                    exclude(group = "androidx.fragment", module = "fragment-ktx")
                    exclude(group = "androidx.preference", module = "preference")
                    exclude(group = "androidx.recyclerview", module = "recyclerview")
                    exclude(group = "androidx.slidingpanelayout", module = "slidingpanelayout")
                    exclude(group = "androidx.swiperefreshlayout", module = "swiperefreshlayout")
                    exclude(group = "com.google.android.material", module = "material")
                }

                // The plain exclude() above is necessary but not sufficient for androidx.core/
                // core-ktx specifically: real, non-SESL-forked leaves (androidx.activity,
                // androidx.compose.ui, emoji2, autofill, window, graphics, savedstate) each
                // hard-depend on a different real core version — confirmed via
                // ResolvedComponentResult that these are genuine dependency edges, not constraints.
                // Once that many independent real-core-version conflicts exist simultaneously in
                // ONE configuration (only happens on :app's own main classpath, which declares real
                // Compose — material3/activity-compose — directly, alongside oneui-design), Gradle's
                // exclude() stops cutting those specific edges, and real core still ends up on the
                // classpath despite the exclude rule above. Declaring the SESL fork as an alternate
                // provider of the real capability and picking a winner per config survives that
                // reliably; non-test configs prefer the SESL fork, test configs keep real AndroidX
                // for Robolectric. On configs where no such pileup exists (e.g. androidTest, whose
                // own test-only libraries pull real core but never sesl core into the same graph),
                // the plain exclude() above already does the job and this capability rule is a
                // no-op — but it must stay scoped identically to the exclude() above rather than
                // narrowed to just the main classpath, since androidTest still needs real core kept
                // out where a SESL alternative does exist (e.g. via :app's own oneui-design edge).
                dependencies {
                    components {
                        withModule("sesl.androidx.core:core") {
                            allVariants { withCapabilities { addCapability("androidx.core", "core", id.version) } }
                        }
                        withModule("sesl.androidx.core:core-ktx") {
                            allVariants { withCapabilities { addCapability("androidx.core", "core-ktx", id.version) } }
                        }
                    }
                }
                configurations.matching { !it.name.startsWith("test", ignoreCase = true) }.configureEach {
                    resolutionStrategy.capabilitiesResolution {
                        withCapability("androidx.core:core") { select(candidates.first { it.id.toString().startsWith("sesl.") }) }
                        withCapability("androidx.core:core-ktx") { select(candidates.first { it.id.toString().startsWith("sesl.") }) }
                    }
                }
                configurations.matching { it.name.startsWith("test", ignoreCase = true) }.configureEach {
                    resolutionStrategy.capabilitiesResolution {
                        withCapability("androidx.core:core") { select(candidates.first { !it.id.toString().startsWith("sesl.") }) }
                        withCapability("androidx.core:core-ktx") { select(candidates.first { !it.id.toString().startsWith("sesl.") }) }
                    }
                }
            }
        }
    }
}
