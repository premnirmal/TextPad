# TextPad

[![Android Build](https://github.com/premnirmal/TextPad/actions/workflows/android.yml/badge.svg)](https://github.com/premnirmal/TextPad/actions/workflows/android.yml)
[![iOS Build](https://github.com/premnirmal/TextPad/actions/workflows/ios.yml/badge.svg)](https://github.com/premnirmal/TextPad/actions/workflows/ios.yml)
[![Detekt](https://github.com/premnirmal/TextPad/actions/workflows/detekt.yml/badge.svg)](https://github.com/premnirmal/TextPad/actions/workflows/detekt.yml)

<a href="https://play.google.com/store/apps/details?id=com.github.premnirmal.textpad" target="_blank">
<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/7/78/Google_Play_Store_badge_EN.svg/500px-Google_Play_Store_badge_EN.svg.png" alt="Get it on Google Play" height="40"/>
</a> 
<a href="https://apps.apple.com/us/app/textpad-editor/6786009829" target="_blank">
<img src="https://developer.apple.com/assets/elements/badges/download-on-the-app-store.svg" alt="Available on the App Store" height="40"/>
</a> 

## App features

TextPad is a minimal plain-text scratchpad. It is now a **Kotlin Multiplatform**
application that shares both its **data layer** and its **Compose Multiplatform UI**
between **Android** and **iOS**.

## Project structure

```
.
├── app/                         # Android application module (shell)
│   └── src/main/                # MainActivity, Glance widget, AndroidManifest, resources
├── shared/                      # Shared Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/          # Shared code for all targets
│       │   ├── kotlin/          # ViewModel, Cache, FileService API, Compose UI, theme
│       │   └── composeResources/# Shared drawables (Compose Resources)
│       ├── androidMain/         # Android-only actuals (FileService, dynamic color)
│       └── iosMain/             # iOS-only code (MainViewController, actuals)
└── iosApp/                      # iOS application (Xcode project, SwiftUI entry point)
    └── TextPadWidget/           # iOS WidgetKit home screen widget extension
```

### Shared (`commonMain`)

* `MainViewModel` – platform-independent `androidx.lifecycle.ViewModel`.
* `data/Cache` – note persistence backed by [multiplatform-settings]
  (`SharedPreferences` on Android, `NSUserDefaults` on iOS).
* `data/AppSettings` – `expect`/`actual` factory for the note `Settings`. On iOS it is
  backed by the App Group suite (`group.com.github.premnirmal.textpad`) so the home
  screen widget shares the same store.
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

## Home screen widget

Both platforms ship a home screen widget that displays the current note using the
same Material 3 surface/on-surface colors and text sizes as the app.

* **Android** (`app/src/main/kotlin/.../widget`): a [Jetpack Glance] `GlanceAppWidget`
  (`TextPadWidget`) reads the cached note from the default `SharedPreferences` and is
  hosted by `TextPadWidgetReceiver`. Tapping the widget opens the app, which refreshes
  the widget on stop.
* **iOS** (`iosApp/TextPadWidget`): a WidgetKit extension reads the note from the shared
  App Group defaults. The main app and the widget both declare the
  `group.com.github.premnirmal.textpad` App Group, and the app reloads widget timelines
  when it moves to the background.

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
[Jetpack Glance]: https://developer.android.com/develop/ui/compose/glance
