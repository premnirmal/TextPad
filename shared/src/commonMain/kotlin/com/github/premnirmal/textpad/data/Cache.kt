package com.github.premnirmal.textpad.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Persists the current note using multiplatform key-value [Settings]
 * (SharedPreferences on Android, NSUserDefaults on iOS).
 */
class Cache(
    private val settings: Settings
) {

    suspend fun saveNote(note: String) {
        withContext(Dispatchers.Default) {
            settings.putString(KEY_NOTE, note)
        }
        // Notify the platform only after the write has completed so the home screen widget
        // re-reads the shared store and shows the latest text instead of a stale value.
        NoteWidgetUpdater.notifyNoteSaved()
    }

    suspend fun getNote(): String = withContext(Dispatchers.Default) {
        settings.getString(KEY_NOTE, "")
    }

    companion object {
        const val KEY_NOTE = "com.github.premnirmal.KEY_NOTE"
    }
}
