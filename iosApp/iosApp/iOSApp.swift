import SwiftUI
import WidgetKit
import ComposeApp

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    init() {
        NoteWidgetUpdater.shared.onNoteSaved = {
            WidgetCenter.shared.reloadAllTimelines()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase != .active {
                WidgetCenter.shared.reloadAllTimelines()
            }
        }
    }
}
