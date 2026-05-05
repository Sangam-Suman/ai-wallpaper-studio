package com.example.aiwallpaper.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aiwallpaper.R
import com.example.aiwallpaper.worker.DailyWallpaperWorker

class WallpaperWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_GENERATE) {
            val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>().build()
            WorkManager.getInstance(context).enqueue(req)
        }
    }

    companion object {
        const val ACTION_GENERATE = "com.example.aiwallpaper.WIDGET_GENERATE"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val intent = Intent(context, WallpaperWidgetProvider::class.java).apply {
                action = ACTION_GENERATE
            }
            val pending = PendingIntent.getBroadcast(
                context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_generate, pending)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
