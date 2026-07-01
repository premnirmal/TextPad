package com.github.premnirmal.textpad.data

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

/**
 * Persists the note in the App Group suite so the WidgetKit extension shares the same
 * store. Falls back to standard defaults if the suite is unavailable.
 */
actual fun createNoteSettings(): Settings {
    val defaults: NSUserDefaults? = NSUserDefaults(suiteName = APP_GROUP_ID)
    return NSUserDefaultsSettings(defaults ?: NSUserDefaults.standardUserDefaults)
}
