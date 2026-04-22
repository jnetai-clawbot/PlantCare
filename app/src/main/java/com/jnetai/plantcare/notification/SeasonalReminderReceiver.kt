package com.jnetai.plantcare.notification

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class SeasonalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val season = intent.getStringExtra("season") ?: "Seasonal"
        val tips = intent.getStringExtra("tips") ?: "Check your plants!"

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SEASONAL)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("🌱 $season Reminders")
            .setContentText(tips)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(tips)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(season.hashCode(), notification)
    }
}