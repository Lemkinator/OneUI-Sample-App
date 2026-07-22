# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```powershell
./gradlew assembleDebug      # build debug APK
./gradlew assembleRelease    # build release APK (falls back to debug signing if no signing props)
./gradlew build              # full build (used in CI)
```

Test suite: unit tests (Kotest/MockK), Robolectric integration tests, Roborazzi screenshot tests, Kover coverage. Run with
`./gradlew testDebugUnitTest`.

Instrumented tests run via Gradle Managed Device (no physical device needed):

```powershell
./gradlew pixel9Api35DebugAndroidTest   # downloads ~1 GB image on first run, cached after
```

The GMD device (`pixel9Api35`: Pixel 9 / API 35 / aosp / x86_64) is declared once in root
`build.gradle.kts` and shared by `:app` instrumented tests and `:benchmarks` baseline profile generation.

## Private Dependencies (Required for Build)

`oneui-design` is hosted on a private GitHub Maven repo. Provide `ghUsername` + `ghAccessToken` (`read:packages` scope) via **one** of
these (checked in order):

1. `github.properties` in project root: `ghUsername=...` / `ghAccessToken=...`
2. `~/.gradle/gradle.properties`: `ghUsername=...` / `ghAccessToken=...`
3. Env vars: `GH_USERNAME` / `GH_ACCESS_TOKEN`

Release signing properties (`releaseStoreFile`, `releaseStorePassword`, `releaseKeyAlias`, `releaseKeyPassword`) use the same lookup order.

## Architecture

Single-module (`:app`) Android app demonstrating OneUI-Design components. Layered architecture (data/domain/ui):

- **`data/`** - `UserSettings`: SharedPreferences-backed store for user settings. Exposes per-field property delegates and a
  `StateFlow<UserSettingsSnapshot>`. All writes assign directly, single- or multi-field: `userSettings.search = "query"`. No batch write
  API — `SharedPreferences` already skips no-op writes for unchanged keys, and no consumer needs atomic multi-field emission.
- **`domain/`** - Use cases with `operator fun invoke()`. Suspend use cases (e.g. `CompleteOnboardingUseCase`) switch to
  `Dispatchers.Default`; flow-based use cases (e.g. `ObserveIconListUseCase`) return a `Flow` directly.
- **`ui/`** - Activities for settings/about/OOBE/pickers; Fragments for main tabs (`TabDesign`, `TabIcons`, `TabPicker`) with nested subtabs
  via ViewPager2. Screens with non-trivial async state use ViewModels (`AboutViewModel`, `SettingsViewModel`, `OOBEViewModel`,
  `SwitchBarViewModel`, `AppPickerViewModel`); simpler screens inject use cases or `UserSettings` directly.
- **`App.kt`** - `@HiltAndroidApp` entry point; `PersistenceModule.kt` - Hilt singleton providing `SharedPreferences` and
  `UserSettings` (with an `@ApplicationScope` `CoroutineScope` for the `StateFlow`).

State collection patterns:

- Fragments: `launchAndRepeatWithViewLifecycle { flow.collectLatest { } }`, or the `collectState` / `collectEvents` shortcuts from
  `ui/util/LifecycleUtils.kt`
- Activities: `collectState(viewModel.state) { }` / `collectEvents(viewModel.events) { }`
- One-shot delayed UI work: `viewLifecycleOwner.lifecycleScope.launch { delay(ms); ... }` (canceled automatically on `onDestroyView`)

ViewBinding uses the `autoCleared` delegate (`ui/util/AutoClearedUtils.kt`) to prevent leaks.

## Robolectric + JUnit 5

**Do not migrate Robolectric tests to JUnit 5.** `org.robolectric.junit.jupiter.RobolectricExtension` does not exist - Robolectric has no
native JUnit 5 support ([issue #3477](https://github.com/robolectric/robolectric/issues/3477)). The community extension
`tech.apter.junit5.jupiter:robolectric-extension` only targets Robolectric 4.14.1, is pre-release, and has no Hilt/Roborazzi support.

`@RunWith(RobolectricTestRunner::class)` + `junit-vintage-engine` is correct. Keep until Robolectric ships native JUnit 5.

**Kover + inline functions**: JUnit 4 + `RobolectricTestRunner` enables JaCoCo SMAP attribution — inlined call-site coverage is mapped back
to the original `inline fun` definition. Simple delegating `inline fun` therefore don't need `@NoCoverage` here (tests calling them cover
the definition via SMAP). The exception is `crossinline` default-value lambdas: the default compiles to a definition-site private static
method that is never invoked, so it always needs `@NoCoverage` regardless of test runner. That is why the `@NoCoverage` footprint here is
smaller than in a JUnit 5 + `RobolectricExtension` setup, where SMAP attribution doesn't fire.

## Settings in Tests

Tests never mock settings — every test uses the real `UserSettings` over an isolated, empty store:

- **Hilt tests** (Robolectric `src/test` and instrumented `src/androidTest`) use `TestSettingsModule` — byte-identical twins in both
  source sets (not `testFixtures`; Hilt's kapt/ksp aggregation silently skips a `@TestInstallIn` module declared in `testFixtures` for the
  Robolectric side) — provides `UserSettings(freshTestPreferences(context), CoroutineScope(SupervisorJob() + Dispatchers.Default))`,
  replacing `PersistenceModule`.
- **Pure-JVM specs** (the ViewModelTests, no Context available) use `fakeUserSettings()` (`app/src/testFixtures`) — a real
  `UserSettings(FakeSharedPreferences(), CoroutineScope(UnconfinedTestDispatcher()))` — `FakeSharedPreferences` fires
  `OnSharedPreferenceChangeListener` correctly, so `.flow` behaves like the real thing; the dispatcher must be unconfined so
  `SharingStarted.Eagerly` propagates synchronously (avoids the `StandardTestDispatcher`-queues-without-running flake `.state.value` reads
  are otherwise exposed to).
- **`freshTestPreferences()`** (`app/src/testFixtures`) returns a UUID-named `SharedPreferences` file, fresh by construction — never a
  fixed name or `.edit().clear()`.
- **`UserSettings.bypassOobe()`** (`app/src/testFixtures`) sets `lastVersionCode`/`acceptedTosVersion` to `Int.MAX_VALUE` so
  `onboardIfNeeded()` never redirects to OOBE in a test.

`app/src/testFixtures` is enabled on `:app` itself (`android.testFixtures.enable = true`) — this app has no shared library to draw test
helpers from, so its own `testFixtures` source set is the only way to share code between `src/test` and `src/androidTest`.

## Dependency Version Policy

Default: use the latest stable version of every dependency.
Renovate keeps minor/patch current; bump majors manually with release-note review.

Known exceptions:

1. Kotlin + KSP lockstep - Renovate's `kotlin` group enforces this
2. Detekt on fresh Kotlin majors. May need alpha until stable catches up
3. Plugin AGP compatibility windows, check before bumping AGP
4. CI emulator images, pin to most stable, not newest

## Static Analysis

Four tools run as part of `./gradlew build`:

- **Spotless** - enforces formatting via ktlint (sole ktlint driver; Detekt has no ktlint wrapper). Fix violations with
  `./gradlew spotlessApply`.
- **Detekt** - static analysis; config at `config/detekt/detekt.yml`. `autoCorrect = false`.
- **Kover** - coverage; verify threshold with `./gradlew koverVerifyDebug`.
- **Konsist** - architecture rules in `app/src/test/java/de/lemke/oneuisample/ArchitectureTest.kt`. Enforces `data/domain/ui` layering. Runs
  as part of `./gradlew test`.

**Pre-commit hook** - blocks commits with formatting violations. Opt in once per clone:

```powershell
git config core.autocrlf input           # Windows: prevents CRLF violations
git config core.hooksPath .githooks
```

The hook runs `spotlessCheck detekt` and exits 1 with a `./gradlew spotlessApply` reminder on failure. It also fails fast with a targeted
message if `core.autocrlf=true` is detected.

**After any change** run the full local CI suite before declaring work done:

```powershell
./gradlew spotlessCheck detekt lintDebug testDebugUnitTest koverVerifyDebug verifyRoborazziDebug pixel9Api35DebugAndroidTest assembleRelease
```

If `spotlessCheck` fails, fix with `./gradlew spotlessApply` then re-run. Screenshot test failures (`verifyRoborazziDebug`) mean the code
change broke a visual. Do not analyze screenshots, ask the user to verify the changes.

**Dependency analysis**, manual hygiene tool (not in CI). Invoke with:

```powershell
./gradlew buildHealth
```

Report at `build/reports/dependency-analysis/build-health-report.txt`. Review unused/misconfigured deps case-by-case.

**ktlint rule overrides**: two rules disabled in `.editorconfig` to match community practice (NowInAndroid, Pokedex both use the inline
form):

- `ktlint_standard_annotation = disabled` - ktlint 1.7+ moves `@Inject` before `constructor` onto its own continuation line,
  doubly-indenting the class body (8 sp instead of 4 sp).
- `ktlint_standard_class-signature = disabled` - in ktlint 1.7+, both rules together enforce the split form; disabling only `annotation` is
  insufficient.

## Key Patterns

**Dependency exclusions** - root `build.gradle.kts` globally excludes `appcompat`, `fragment`, `recyclerview`, `material`, `viewpager2`, and
others from all subprojects - `oneui-design` bundles its own versions. Never add these as explicit dependencies.

**Navigation** - Navigation Component (`main_navigation.xml`) handles fragment destinations. Lateral activities are launched via
`startActivity(Intent(...))`. `NavigationView.onNavigationSingleClick` debounces rapid taps.

**Compose is minimal** - only the `AboutLibraries` screen uses Compose + Material3; all other UI is View-based with OneUI components (
`ic_oui_*` drawables, OneUI `DrawerLayout`, etc.).
