package com.github.premnirmal.textpad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.premnirmal.textpad.data.Cache
import com.github.premnirmal.textpad.data.FileService
import kotlin.concurrent.Volatile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val cache: Cache
) : ViewModel() {

    // Emits content that should replace the editor's text (cache restore, file open).
    // A replaying SharedFlow is used instead of a StateFlow so that every emission is
    // delivered to the UI even when the value is identical to a previous one. This keeps
    // opening a file working after the editor has diverged from the last emitted value
    // (for example after clearing the text and re-opening the same file).
    val editorContent: Flow<String>
        get() = _editorContent
    private val _editorContent = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 1)

    val messageState: Flow<String>
        get() = _messageState
    private val _messageState = MutableSharedFlow<String>()

    // Guards against the initially-empty editor wiping a cached note during startup: the
    // first restore from the cache must complete before an empty note is allowed to persist.
    // Once loaded, manually clearing all text does persist the empty note so the widget updates.
    @Volatile
    private var isLoaded = false

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val cachedNote = cache.getNote()
            if (cachedNote.isNotEmpty()) {
                _editorContent.emit(cachedNote.trim() + "\n")
            }
            isLoaded = true
        }
    }

    fun updateCache(note: String) {
        // Skip empty writes until the cached note has been restored, otherwise the empty
        // editor state at startup would overwrite it before the user sees their text.
        if (note.trim().isEmpty() && !isLoaded) return
        viewModelScope.launch(Dispatchers.Default) {
            cache.saveNote(note)
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.Default) {
            cache.saveNote("")
        }
    }

    // File IO can fail with a variety of platform-specific exceptions; catching broadly
    // and surfacing a single user message is intentional here.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun open(fileService: FileService) {
        viewModelScope.launch {
            try {
                val text = fileService.openFile() ?: return@launch
                _editorContent.emit(text.trim() + "\n")
                _messageState.emit("Opened")
            } catch (e: Exception) {
                _messageState.emit("Error opening file")
            }
        }
    }

    // File IO can fail with a variety of platform-specific exceptions; catching broadly
    // and surfacing a single user message is intentional here.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun save(fileService: FileService) {
        viewModelScope.launch {
            val note = cache.getNote().trim()
            try {
                val success = fileService.saveFile(suggestedName = "File.txt", content = note)
                if (success) _messageState.emit("Saved")
            } catch (e: Exception) {
                _messageState.emit("Error saving file")
            }
        }
    }
}
