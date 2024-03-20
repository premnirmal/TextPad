package com.github.premnirmal.textpad

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var cache: Cache

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.appComponent.inject(this)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.edit_text)
        val text = cache.getNote().trim() + "\n"
        editText.setText(text)
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        editText.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onPause() {
        super.onPause()
        cache.saveNote(editText.text.toString().trim())
    }
}
