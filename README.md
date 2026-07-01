# TextPad

[![Android Build](https://github.com/premnirmal/TextPad/actions/workflows/android.yml/badge.svg)](https://github.com/premnirmal/TextPad/actions/workflows/android.yml)
[![iOS Build](https://github.com/premnirmal/TextPad/actions/workflows/ios.yml/badge.svg)](https://github.com/premnirmal/TextPad/actions/workflows/ios.yml)
[![Detekt](https://github.com/premnirmal/TextPad/actions/workflows/detekt.yml/badge.svg)](https://github.com/premnirmal/TextPad/actions/workflows/detekt.yml)

TextPad is a minimal plain-text scratchpad. It is now a **Kotlin Multiplatform**
application that shares both its **data layer** and its **Compose Multiplatform UI**
between **Android** and **iOS**.

## Project structure

```
.
├── app/                         # Android application module (shell)
│   └── src/main/                # MainActivity, AndroidManifest, Android resources
├── shared/                      # Shared Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/          # Shared code for all targets
│       │   ├── kotlin/          # ViewModel, Cache, FileService API, Compose UI, theme
│       │   └── composeResources/# Shared drawables (Compose Resources)
│       ├── androidMain/         # Android-only actuals (FileService, dynamic color)
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

* **Android**: the `app/` module hosts `MainActivity`, which renders the shared
  `App()`; `shared/src/androidMain` provides the `FileService` (Storage Access
  Framework) and dynamic color (`dynamicLight/DarkColorScheme`) actuals.
* **iOS** (`shared/src/iosMain`): `MainViewController()` exposes `App()` to SwiftUI
  through a `ComposeUIViewController`; `FileService` uses
  `UIDocumentPickerViewController`.

## Building

### Android

```bash
./gradlew :app:assembleDebug
```

Requires access to Google's Maven repository (`dl.google.com`) for the Android
Gradle Plugin and AndroidX artifacts.

### iOS

The Xcode project is generated from `iosApp/project.yml` with
[XcodeGen](https://github.com/yonaskolb/XcodeGen) and is not committed. Generate
it first (install XcodeGen via `brew install xcodegen` if needed):

```bash
cd iosApp && xcodegen generate
```

Then open `iosApp/iosApp.xcodeproj` in Xcode (macOS required) and run. The
project's "Compile Kotlin Framework" build phase builds the shared framework
automatically.

Set a development team via `iosApp/Configuration/Config.xcconfig` (`TEAM_ID`).

[multiplatform-settings]: https://github.com/russhwolf/multiplatform-settings
