# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```powershell
./gradlew assembleDebug      # build debug APK
./gradlew assembleRelease    # build release APK (falls back to debug signing if no signing props)
./gradlew build              # full build (used in CI)
./gradlew lint
```

Test suite: unit tests (Kotest/MockK), Robolectric integration tests, Roborazzi screenshot tests, Kover coverage. Run with `./gradlew testDebugUnitTest`.

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

**Do not migrate Robolectric tests to JUnit 5.** `org.robolectric.junit.jupiter.RobolectricExtension` does not exist — Robolectric has no
native JUnit 5 support ([issue #3477](https://github.com/robolectric/robolectric/issues/3477)). The community extension
`tech.apter.junit5.jupiter:robolectric-extension` only targets Robolectric 4.14.1, is pre-release, and has no Hilt/Roborazzi support.

`@RunWith(RobolectricTestRunner::class)` + `junit-vintage-engine` is correct. Keep until Robolectric ships native JUnit 5.

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

- **Spotless** — enforces formatting via ktlint (sole ktlint driver; Detekt has no ktlint wrapper). Fix violations with `./gradlew spotlessApply`.
- **Detekt** — static analysis; config at `config/detekt/detekt.yml`. `autoCorrect = false` so fixes are manual.
- **Kover** — coverage; verify threshold with `./gradlew koverVerifyDebug`.
- **Konsist** — architecture rules in `app/src/test/java/de/lemke/oneuisample/ArchitectureTest.kt`. Enforces `data/domain/ui` layering. Runs as part of `./gradlew test`.

**Pre-commit hook** — blocks commits with formatting violations. Opt in once per clone:

```powershell
git config core.autocrlf input           # Windows: prevents CRLF violations
git config core.hooksPath .githooks
```

The hook runs `spotlessCheck` and exits 1 with a `./gradlew spotlessApply` reminder on failure. It also fails fast with a targeted message if `core.autocrlf=true` is detected.

**After any change** — run the full local CI suite before declaring work done:

```powershell
./gradlew spotlessCheck detekt lintDebug testDebugUnitTest koverVerifyDebug verifyRoborazziDebug
```

If `spotlessCheck` fails, fix with `./gradlew spotlessApply` then re-run. Screenshot test failures (`verifyRoborazziDebug`) mean the code change broke a visual — do not analyze screenshots, ask the user to verify the changes.

**Dependency analysis** — manual hygiene tool (not in CI). Invoke with:

```powershell
./gradlew buildHealth
```

Report at `build/reports/dependency-analysis/build-health-report.txt`. Review unused/misconfigured deps case-by-case.

**ktlint rule overrides** — two rules disabled in `.editorconfig` to match community practice (NowInAndroid, Pokedex both use the inline form):

- `ktlint_standard_annotation = disabled` — ktlint 1.7+ moves `@Inject` before `constructor` onto its own continuation line, doubly-indenting the class body (8 sp instead of 4 sp).
- `ktlint_standard_class-signature = disabled` — in ktlint 1.7+, both rules together enforce the split form; disabling only `annotation` is insufficient.

**Important**: when upgrading ktlint, files already formatted in the ktlint-native (8-space) style will NOT be automatically reverted by `spotlessApply` — ktlint only flags violations of *enabled* rules. If you re-enable these rules and then disable them again, you must manually restore the inline form and re-run `spotlessApply`. See git history for the migration pattern.

**IDE formatter (Ctrl+Alt+L) vs spotlessApply** — these ARE in sync. The ktlint IntelliJ plugin (`.idea/ktlint-plugin.xml`, mode `DISTRACT_FREE`) runs ktlint as a **post-processor after** IntelliJ's native formatter. Flow: IntelliJ formats → plugin runs ktlint on the result → final output matches `spotlessApply` exactly. IntelliJ never "learns" ktlint rules; ktlint just fixes IntelliJ's output. If the plugin mode is changed to `MANUAL`, this breaks — keep `DISTRACT_FREE`.

## Key Patterns

**Dependency exclusions** — root `build.gradle.kts` globally excludes `appcompat`, `fragment`, `recyclerview`, `material`, `viewpager2`, and
others from all subprojects — `oneui-design` bundles its own versions. Never add these as explicit dependencies.

**Navigation** — Navigation Component (`main_navigation.xml`) handles fragment destinations. Lateral activities are launched via
`startActivity(Intent(...))`. `NavigationView.onNavigationSingleClick` debounces rapid taps.

**Compose is minimal** — only the `AboutLibraries` screen uses Compose + Material3; all other UI is View-based with OneUI components (
`ic_oui_*` drawables, OneUI `DrawerLayout`, etc.).
