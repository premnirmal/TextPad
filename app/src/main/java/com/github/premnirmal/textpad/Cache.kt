package com.github.premnirmal.textpad

import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Cache @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val KEY_NOTE = "com.github.premnirmal.KEY_NOTE"
    }


    suspend fun saveNote(note: String) {
        withContext(Dispatchers.IO) { sharedPreferences.edit(commit = true) { putString(KEY_NOTE, note) } }
    }

    suspend fun getNote(): String = withContext(Dispatchers.IO) { sharedPreferences.getString(KEY_NOTE, "") ?: "" }

}