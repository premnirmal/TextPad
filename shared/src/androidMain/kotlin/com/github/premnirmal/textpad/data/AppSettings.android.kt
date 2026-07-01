package com.github.premnirmal.textpad.data

import com.russhwolf.settings.Settings

/** Uses the default `SharedPreferences`-backed store on Android. */
actual fun createNoteSettings(): Settings = Settings()
