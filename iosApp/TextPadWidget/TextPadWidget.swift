import SwiftUI
import WidgetKit

// Shared with the Kotlin data layer:
// - App Group suite: see APP_GROUP_ID in shared/.../data/AppSettings.kt
// - Note key: see Cache.KEY_NOTE in shared/.../data/Cache.kt
private let appGroupId = "group.com.github.premnirmal.textpad"
private let noteKey = "com.github.premnirmal.KEY_NOTE"

/// Reads the note persisted by the app from the shared App Group defaults.
private func loadNote() -> String {
    let defaults = UserDefaults(suiteName: appGroupId)
    let note = defaults?.string(forKey: noteKey) ?? ""
    return note.trimmingCharacters(in: .whitespacesAndNewlines)
}

struct NoteEntry: TimelineEntry {
    let date: Date
    let note: String
}

struct NoteProvider: TimelineProvider {
    func placeholder(in context: Context) -> NoteEntry {
        NoteEntry(date: Date(), note: "")
    }

    func getSnapshot(in context: Context, completion: @escaping (NoteEntry) -> Void) {
        completion(NoteEntry(date: Date(), note: loadNote()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<NoteEntry>) -> Void) {
        let entry = NoteEntry(date: Date(), note: loadNote())
        // Refresh periodically; the app also nudges WidgetKit on save via reloadAllTimelines.
        let next = Calendar.current.date(byAdding: .minute, value: 30, to: Date())
            ?? Date().addingTimeInterval(30 * 60)
        completion(Timeline(entries: [entry], policy: .after(next)))
    }
}

// Material 3 surface palette, matching the shared Compose theme (theme/Color.kt).
private extension Color {
    static let appSurface = Color(UIColor { traits in
        traits.userInterfaceStyle == .dark
            ? UIColor(red: 0x12 / 255, green: 0x13 / 255, blue: 0x18 / 255, alpha: 1)
            : UIColor(red: 0xFB / 255, green: 0xF8 / 255, blue: 0xFF / 255, alpha: 1)
    })
    static let appOnSurface = Color(UIColor { traits in
        traits.userInterfaceStyle == .dark
            ? UIColor(red: 0xE3 / 255, green: 0xE1 / 255, blue: 0xE9 / 255, alpha: 1)
            : UIColor(red: 0x1B / 255, green: 0x1B / 255, blue: 0x21 / 255, alpha: 1)
    })
    static let appOnSurfaceVariant = Color(UIColor { traits in
        traits.userInterfaceStyle == .dark
            ? UIColor(red: 0xC6 / 255, green: 0xC5 / 255, blue: 0xD0 / 255, alpha: 1)
            : UIColor(red: 0x45 / 255, green: 0x46 / 255, blue: 0x4F / 255, alpha: 1)
    })
}

struct TextPadWidgetEntryView: View {
    var entry: NoteProvider.Entry

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "note.text")
                    .font(.system(size: 20))
                    .foregroundColor(.appOnSurface)
                Text("TextPad")
                    .font(.system(size: 16, weight: .bold)) // matches AppTypography.titleMedium
                    .foregroundColor(.appOnSurface)
            }
            if entry.note.isEmpty {
                Text("No text yet")
                    .font(.system(size: 14)) // matches AppTypography.bodyMedium
                    .foregroundColor(.appOnSurfaceVariant)
            } else {
                Text(entry.note)
                    .font(.system(size: 14)) // matches AppTypography.bodyMedium
                    .foregroundColor(.appOnSurface)
                    .lineLimit(nil)
            }
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .containerBackground(Color.appSurface, for: .widget)
    }
}

struct TextPadWidget: Widget {
    let kind = "TextPadWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: NoteProvider()) { entry in
            TextPadWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("TextPad")
        .description("Shows your current TextPad note.")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

@main
struct TextPadWidgetBundle: WidgetBundle {
    var body: some Widget {
        TextPadWidget()
    }
}
