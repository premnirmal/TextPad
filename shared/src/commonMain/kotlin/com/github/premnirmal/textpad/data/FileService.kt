package com.github.premnirmal.textpad.data

import androidx.compose.runtime.Composable

/**
 * Platform abstraction for opening and saving plain-text files using the
 * native document picker.
 */
interface FileService {

    /** Presents a picker to choose a text file and returns its contents, or null if cancelled. */
    suspend fun openFile(): String?

    /** Presents a picker to save [content] to a file, returning true on success. */
    suspend fun saveFile(suggestedName: String, content: String): Boolean
}

/** Provides a platform [FileService] bound to the current UI context. */
@Composable
expect fun rememberFileService(): FileService
