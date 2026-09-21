package com.github.premnirmal.textpad

import android.app.Application
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.github.premnirmal.textpad.data.NoteWidgetUpdater
import com.github.premnirmal.textpad.widget.TextPadWidget
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "TextPadApplication"

class TextPadApplication : Application() {

    private val widgetExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w(TAG, "Unable to update TextPad widget", throwable)
    }
    private val widgetScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + widgetExceptionHandler
    )
    private val widget = TextPadWidget()

    override fun onCreate() {
        super.onCreate()
        NoteWidgetUpdater.onNoteSaved = {
            widgetScope.launch {
                widget.updateAll(applicationContext)
            }
        }
    }
}
