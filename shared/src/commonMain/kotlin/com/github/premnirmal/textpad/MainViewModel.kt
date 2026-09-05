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

    val editorContent: Flow<String>
        get() = _editorContent
    private val _editorContent = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 1)

    val messageState: Flow<String>
        get() = _messageState
    private val _messageState = MutableSharedFlow<String>()

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
