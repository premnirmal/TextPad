package com.github.premnirmal.textpad

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cache: Cache
) : ViewModel() {

    val note: StateFlow<String>
        get() = _note
    private val _note = MutableStateFlow("")

    val messageState: Flow<String>
        get() = _messageState
    private val _messageState = MutableSharedFlow<String>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedNote = cache.getNote()
            if (cachedNote.isNotEmpty()) {
                _note.emit(cachedNote.trim() + "\n")
            }
        }
    }

    fun updateCache(note: String) {
        if (note.trim().isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            cache.saveNote(note)
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            cache.saveNote("")
        }
    }

    fun open(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val contentResolver = context.applicationContext.contentResolver
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val text: String = reader.readText()
                        _note.emit(text.trim() + "\n")
                    }
                }
                _messageState.emit("Opened")
            } catch (e: IOException) {
                Timber.e(e)
                _messageState.emit("Error opening file")
            }
        }
    }

    fun save(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = cache.getNote().trim()
            try {
                val contentResolver = context.applicationContext.contentResolver
                contentResolver.openFileDescriptor(uri, "rwt")?.use {
                    FileOutputStream(it.fileDescriptor).use { fileOutputStream ->
                        fileOutputStream.write(note.toByteArray())
                    }
                }
                _messageState.emit("Saved")
            } catch (e: IOException) {
                Timber.e(e)
                _messageState.emit("Error saving file")
            }
        }
    }
}