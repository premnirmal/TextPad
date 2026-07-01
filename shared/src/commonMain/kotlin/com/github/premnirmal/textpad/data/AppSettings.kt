package com.github.premnirmal.textpad.data

import com.russhwolf.settings.Settings

/**
 * App Group identifier used to share the persisted note between the iOS app and its
 * home screen widget. Must match the App Group configured on both iOS targets.
 */
const val APP_GROUP_ID = "group.com.github.premnirmal.textpad"

/**
 * Creates the [Settings] instance used to persist the current note.
 *
 * On Android this is the default `SharedPreferences`-backed store; on iOS it is backed
 * by the App Group suite so the WidgetKit extension can read the same value.
 */
expect fun createNoteSettings(): Settings
