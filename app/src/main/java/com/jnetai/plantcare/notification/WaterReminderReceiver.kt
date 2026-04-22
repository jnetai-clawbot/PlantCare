package com.jnetai.plantcare.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jnetai.plantcare.MainActivity
import com.jnetai.plantcare.R

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getLongExtra("plantId", -1)
        val plantName = intent.getStringExtra("plantName") ?: "Your plant"

        val notification = buildNotification(context, plantId, plantName)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(plantId.toInt(), notification)
    }

    private fun buildNotification(context: Context, plantId: Long, plantName: String): Notification {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("plantId", plantId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            plantId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationHelper.CHANNEL_WATER)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle(context.getString(R.string.watering_notification_title))
            .setContentText(context.getString(R.string.watering_notification_text, plantName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
    }

    companion object {
        fun schedule(context: Context, plantId: Long, plantName: String, intervalDays: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WaterReminderReceiver::class.java).apply {
                putExtra("plantId", plantId)
                putExtra("plantName", plantName)
                action = "com.jnetai.plantcare.WATER_REMINDER_$plantId"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val intervalMs = intervalDays.toLong() * 24 * 60 * 60 * 1000
            val triggerAt = System.currentTimeMillis() + intervalMs

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }

        fun cancel(context: Context, plantId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WaterReminderReceiver::class.java).apply {
                action = "com.jnetai.plantcare.WATER_REMINDER_$plantId"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}