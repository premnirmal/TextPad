package com.github.premnirmal.textpad.data

/**
 * Bridge that lets platform code refresh the home screen note widget immediately after
 * the note has been persisted.
 *
 * The note is saved asynchronously (a debounced write on a background dispatcher), so the
 * platform must only re-read the shared store once the newest value is on disk. iOS
 * registers a listener here that reloads the WidgetKit timelines; invoking it *after* the
 * write completes guarantees the widget reads the latest text instead of a stale value.
 *
 * Android leaves the listener unset because its Glance widget is refreshed through the
 * AppWidget update mechanism.
 */
object NoteWidgetUpdater {

    /**
     * Invoked right after a note is written to the shared store.
     *
     * May be called on any thread (the write runs on a background dispatcher), so the
     * platform listener must be safe to invoke off the main thread. WidgetKit's
     * `WidgetCenter.reloadAllTimelines()` used by the iOS listener is thread-safe.
     */
    var onNoteSaved: (() -> Unit)? = null

    fun notifyNoteSaved() {
        onNoteSaved?.invoke()
    }
}
