package com.leafvibe.leaftracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONArray

class LeafTrackerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateOne(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, LeafTrackerWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.leaftracker_widget)
        val raw = context.getSharedPreferences("leaftracker_widget", Context.MODE_PRIVATE)
            .getString("events", "[]") ?: "[]"
        val events = try { JSONArray(raw) } catch (_: Exception) { JSONArray() }
        val lines = (0 until minOf(events.length(), 4)).map { index ->
            val event = events.getJSONObject(index)
            val date = event.optString("date")
            val time = event.optString("time")
            "• ${event.optString("title")}\n  $date ${time}".trim()
        }
        views.setTextViewText(R.id.widget_events,
            if (lines.isEmpty()) "Aucun événement choisi. Sélectionne « Afficher sur le widget » dans un événement." else lines.joinToString("\n\n"))
        val open = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root, pending)
        manager.updateAppWidget(id, views)
    }
}