package com.github.premnirmal.textpad.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.github.premnirmal.textpad.MainActivity
import com.github.premnirmal.textpad.R
import com.github.premnirmal.textpad.data.Cache

/**
 * Home screen widget that mirrors the current note. Colors and text sizes are kept
 * consistent with the shared Compose app: the widget uses the same surface/onSurface
 * palette (Material 3 light/dark) and the app's body (14sp) and title (16sp) sizes.
 */
class TextPadWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val note = readNote(context)
        provideContent {
            WidgetContent(note)
        }
    }

    private fun readNote(context: Context): String {
        // multiplatform-settings-no-arg persists via the default SharedPreferences,
        // whose file name is "<packageName>_preferences"; read the same store here.
        val prefs = context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE,
        )
        return prefs.getString(Cache.KEY_NOTE, "").orEmpty().trim()
    }
}

@androidx.compose.runtime.Composable
private fun WidgetContent(note: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surfaceColor)
            .cornerRadius(20.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = GlanceModifier.size(24.dp),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "TextPad",
                style = TextStyle(
                    color = onSurfaceColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        // A non-scrolling Column is used instead of LazyColumn: Glance's LazyColumn is
        // backed by a ListView that always renders a scrollbar with no API to hide it.
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = note.ifEmpty { "No text yet" },
                style = TextStyle(
                    color = if (note.isEmpty()) onSurfaceVariantColor else onSurfaceColor,
                    fontSize = 14.sp,
                ),
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionStartActivity<MainActivity>()),
            )
        }
    }
}

// App Material 3 surface palette (light / dark), matching shared theme/Color.kt.
private val surfaceColor = ColorProvider(
    day = Color(0xFFFBF8FF),
    night = Color(0xFF121318),
)
private val onSurfaceColor = ColorProvider(
    day = Color(0xFF1B1B21),
    night = Color(0xFFE3E1E9),
)
private val onSurfaceVariantColor = ColorProvider(
    day = Color(0xFF45464F),
    night = Color(0xFFC6C5D0),
)
