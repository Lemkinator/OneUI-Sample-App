# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```powershell
./gradlew assembleDebug      # build debug APK
./gradlew assembleRelease    # build release APK (falls back to debug signing if no signing props)
./gradlew build              # full build (used in CI)
./gradlew lint
```

No unit tests exist in this project.

## Private Dependencies (Required for Build)

`oneui-design` is hosted on a private GitHub Maven repo. Provide `ghUsername` + `ghAccessToken` (`read:packages` scope) via **one** of these (checked in order):
1. `github.properties` in project root: `ghUsername=...` / `ghAccessToken=...`
2. `~/.gradle/gradle.properties`: `ghUsername=...` / `ghAccessToken=...`
3. Env vars: `GH_USERNAME` / `GH_ACCESS_TOKEN`

Release signing properties (`releaseStoreFile`, `releaseStorePassword`, `releaseKeyAlias`, `releaseKeyPassword`) use the same lookup order.

## Architecture

Single-module (`:app`) Android app demonstrating OneUI-Design components. Layered architecture (data/domain/ui) without ViewModels — Activities and Fragments inject use cases directly:

- **`data/`** — `UserSettingsRepository`: DataStore Preferences CRUD
- **`domain/`** — `suspend operator fun invoke()` use cases; each switches to `Dispatchers.Default`. `UpdateUserSettingsUseCase` takes `(UserSettings) -> UserSettings`; callers use `.copy(field = value)`
- **`ui/`** — Activities for settings/about/OOBE/pickers; Fragments for main tabs (`TabDesign`, `TabIcons`, `TabPicker`) with nested subtabs via ViewPager2
- **`App.kt`** — `@HiltAndroidApp` entry point; `PersistenceModule.kt` — Hilt singleton providing `DataStore<Preferences>`

State collected via `flowWithLifecycle(lifecycle).collectLatest { }` in `lifecycleScope`. ViewBinding uses the `autoCleared` delegate (`ui/util/AutoClearedUtils.kt`) to prevent leaks.

## Key Patterns

**Dependency exclusions** — root `build.gradle.kts` globally excludes `appcompat`, `fragment`, `recyclerview`, `material`, `viewpager2`, and others from all subprojects — `oneui-design` bundles its own versions. Never add these as explicit dependencies.

**Navigation** — Navigation Component (`main_navigation.xml`) handles fragment destinations. Lateral activities are launched via `startActivity(Intent(...))`. `NavigationView.onNavigationSingleClick` debounces rapid taps.

**Compose is minimal** — only the `AboutLibraries` screen uses Compose + Material3; all other UI is View-based with OneUI components (`ic_oui_*` drawables, OneUI `DrawerLayout`, etc.).
