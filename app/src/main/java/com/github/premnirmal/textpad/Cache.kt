package com.github.premnirmal.textpad

import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit

class Cache @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val KEY_NOTE = "com.github.premnirmal.KEY_NOTE"
    }


    fun saveNote(note: String) {
        sharedPreferences.edit { putString(KEY_NOTE, note) }
    }

    fun getNote(): String = sharedPreferences.getString(KEY_NOTE, "")!!

}