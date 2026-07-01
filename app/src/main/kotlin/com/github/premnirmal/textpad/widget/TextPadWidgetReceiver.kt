package com.github.premnirmal.textpad.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Hosts [TextPadWidget] on the home screen and receives widget update broadcasts. */
class TextPadWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TextPadWidget()
}
