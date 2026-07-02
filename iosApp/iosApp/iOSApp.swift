import SwiftUI
import WidgetKit
import ComposeApp

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    init() {
        // Refresh the home screen widget as soon as the note is persisted. Reloading here
        // (rather than only on scene-phase changes) guarantees the widget re-reads the
        // shared App Group store after the latest text has been written, so it never shows
        // stale content.
        NoteWidgetUpdater.shared.onNoteSaved = {
            WidgetCenter.shared.reloadAllTimelines()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { _, newPhase in
            // Refresh the home screen widget with the latest note when leaving the app.
            if newPhase != .active {
                WidgetCenter.shared.reloadAllTimelines()
            }
        }
    }
}
