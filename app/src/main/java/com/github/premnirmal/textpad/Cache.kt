package com.github.premnirmal.textpad

import android.content.SharedPreferences
import javax.inject.Inject

class Cache @Inject constructor() {

    companion object {
        const val KEY_NOTE = "com.github.premnirmal.KEY_NOTE"
    }

    init {
        Injector.appComponent.inject(this)
    }

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    fun saveNote(note: String) {
        sharedPreferences.edit().putString(KEY_NOTE, note).apply()
    }

    fun getNote(): String = sharedPreferences.getString(KEY_NOTE, "")!!

}