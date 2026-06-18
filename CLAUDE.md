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

Single-module (`:app`) Android app demonstrating OneUI-Design components. Layered architecture (data/domain/ui) without ViewModels —
Activities and Fragments inject use cases directly:

- **`data/`** — `UserSettingsRepository`: DataStore Preferences CRUD
- **`domain/`** — `suspend operator fun invoke()` use cases; each switches to `Dispatchers.Default`. `UpdateUserSettingsUseCase` takes
  `(UserSettings) -> UserSettings`; callers use `.copy(field = value)`
- **`ui/`** — Activities for settings/about/OOBE/pickers; Fragments for main tabs (`TabDesign`, `TabIcons`, `TabPicker`) with nested subtabs
  via ViewPager2
- **`App.kt`** — `@HiltAndroidApp` entry point; `PersistenceModule.kt` — Hilt singleton providing `DataStore<Preferences>`

State collected via `flowWithLifecycle(lifecycle).collectLatest { }` in `lifecycleScope`. ViewBinding uses the `autoCleared` delegate (
`ui/util/AutoClearedUtils.kt`) to prevent leaks.

## Robolectric + JUnit 5

`@RunWith(RobolectricTestRunner::class)` + `junit-vintage-engine` is correct — Robolectric has no native JUnit 5 support. Keep until
Robolectric ships native JUnit 5.

## Dependency Version Policy

Default: use the latest stable version of every dependency.
Renovate keeps minor/patch current; bump majors manually with release-note review.

Known exceptions:

1. Kotlin + KSP lockstep — Renovate's `kotlin` group enforces this
2. Detekt on fresh Kotlin majors — may need alpha until stable catches up
3. Plugin AGP compatibility windows — check before bumping AGP
4. CI emulator images — pin to most stable, not newest
5. benchmark-macro — use 1.5.0-alpha06+ with AGP 9.x until 1.5.0 stable

## Static Analysis

Four tools run as part of `./gradlew build`:

- **Spotless** — enforces formatting via ktlint (sole ktlint driver; Detekt has no ktlint wrapper). Fix violations with
  `./gradlew spotlessApply`.
- **Detekt** — static analysis; config at `config/detekt/detekt.yml`. `autoCorrect = false` — fixes are manual.
- **Kover** — 95% INSTRUCTION + BRANCH coverage required. Verify: `./gradlew koverVerifyDebug`.
- **Konsist** — architecture rules in `app/src/test/java/de/lemke/oneuisample/ArchitectureTest.kt`. Enforces `data/domain/ui` layering. Runs
  as part of `./gradlew test`.

**Pre-commit hook** — blocks commits with formatting violations. Opt in once per clone:

```powershell
git config core.autocrlf input           # Windows: prevents CRLF violations
git config core.hooksPath .githooks
```

The hook runs `spotlessCheck` and exits 1 with a `./gradlew spotlessApply` reminder on failure. It also fails fast with a targeted message
if `core.autocrlf=true` is detected.

**After any change** — run the full local CI suite before declaring work done:

```powershell
./gradlew spotlessCheck detekt lintDebug testDebugUnitTest koverVerifyDebug verifyRoborazziDebug pixel9Api35DebugAndroidTest assembleRelease
```

If `spotlessCheck` fails, fix with `./gradlew spotlessApply` then re-run. Screenshot test failures (`verifyRoborazziDebug`) mean the code
change broke a visual — do not analyze screenshots, ask the user to verify the changes.

**Dependency analysis** — manual hygiene tool (not in CI). Invoke with:

```powershell
./gradlew buildHealth
```

Report at `build/reports/dependency-analysis/build-health-report.txt`. Review unused/misconfigured deps case-by-case.

**ktlint rule overrides** — two rules disabled in `.editorconfig` to match community practice (NowInAndroid, Pokedex both use the inline
form):

- `ktlint_standard_annotation = disabled` — ktlint 1.7+ moves `@Inject` before `constructor` onto its own continuation line,
  doubly-indenting the class body (8 sp instead of 4 sp).
- `ktlint_standard_class-signature = disabled` — in ktlint 1.7+, both rules together enforce the split form; disabling only `annotation` is
  insufficient.

## Key Patterns

**Dependency exclusions** — root `build.gradle.kts` globally excludes `appcompat`, `fragment`, `recyclerview`, `material`, `viewpager2`, and
others from all subprojects — `oneui-design` bundles its own versions. Never add these as explicit dependencies.

**Navigation** — Navigation Component (`main_navigation.xml`) handles fragment destinations. Lateral activities are launched via
`startActivity(Intent(...))`. `NavigationView.onNavigationSingleClick` debounces rapid taps.

**Compose is minimal** — only the `AboutLibraries` screen uses Compose + Material3; all other UI is View-based with OneUI components (
`ic_oui_*` drawables, OneUI `DrawerLayout`, etc.).
