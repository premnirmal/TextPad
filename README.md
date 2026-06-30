# TextPad

TextPad is a minimal plain-text scratchpad. It is now a **Kotlin Multiplatform**
application that shares both its **data layer** and its **Compose Multiplatform UI**
between **Android** and **iOS**.

## Project structure

```
.
├── composeApp/                  # Shared Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/          # Shared code for all targets
│       │   ├── kotlin/          # ViewModel, Cache, FileService API, Compose UI, theme
│       │   └── composeResources/# Shared drawables (Compose Resources)
│       ├── androidMain/         # Android-only code (MainActivity, actuals, res, manifest)
│       └── iosMain/             # iOS-only code (MainViewController, actuals)
└── iosApp/                      # iOS application (Xcode project, SwiftUI entry point)
```

### Shared (`commonMain`)

* `MainViewModel` – platform-independent `androidx.lifecycle.ViewModel`.
* `data/Cache` – note persistence backed by [multiplatform-settings]
  (`SharedPreferences` on Android, `NSUserDefaults` on iOS).
* `data/FileService` – `expect` API for open/save, plus a `rememberFileService()`
  composable provided per platform.
* `ui/App` – the entire Compose Multiplatform UI (top bar, editor, FAB menu).
* `theme/` – Material 3 color scheme and typography. `dynamicColorSchemeOrNull`
  is an `expect`/`actual` hook (Material You on Android 12+, branded scheme on iOS).

### Platform code

* **Android** (`androidMain`): `MainActivity` hosts `App()`; `FileService` uses the
  Storage Access Framework; dynamic color uses `dynamicLight/DarkColorScheme`.
* **iOS** (`iosMain`): `MainViewController()` exposes `App()` to SwiftUI through a
  `ComposeUIViewController`; `FileService` uses `UIDocumentPickerViewController`.

## Building

### Android

```bash
./gradlew :composeApp:assembleDebug
```

Requires access to Google's Maven repository (`dl.google.com`) for the Android
Gradle Plugin and AndroidX artifacts.

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode (macOS required) and run, or build the
shared framework first:

```bash
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Set a development team via `iosApp/Configuration/Config.xcconfig` (`TEAM_ID`).

[multiplatform-settings]: https://github.com/russhwolf/multiplatform-settings
