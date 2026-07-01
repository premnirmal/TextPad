import SwiftUI
import WidgetKit

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

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
