package com.github.premnirmal.textpad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.premnirmal.textpad.data.Cache
import com.github.premnirmal.textpad.data.FileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val cache: Cache
) : ViewModel() {

    val note: StateFlow<String>
        get() = _note
    private val _note = MutableStateFlow("")

    val messageState: Flow<String>
        get() = _messageState
    private val _messageState = MutableSharedFlow<String>()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val cachedNote = cache.getNote()
            if (cachedNote.isNotEmpty()) {
                _note.emit(cachedNote.trim() + "\n")
            }
        }
    }

    fun updateCache(note: String) {
        if (note.trim().isEmpty()) return
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
                _note.emit(text.trim() + "\n")
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
