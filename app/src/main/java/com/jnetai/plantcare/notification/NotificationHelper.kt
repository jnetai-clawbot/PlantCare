package com.jnetai.plantcare.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.jnetai.plantcare.R

object NotificationHelper {
    const val CHANNEL_WATER = "water_reminders"
    const val CHANNEL_SEASONAL = "seasonal_reminders"
    const val REQUEST_WATER = 1001
    const val REQUEST_SEASONAL = 1002

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val waterChannel = NotificationChannel(
                CHANNEL_WATER,
                context.getString(R.string.watering_channel),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to water your plants"
                enableVibration(true)
            }

            val seasonalChannel = NotificationChannel(
                CHANNEL_SEASONAL,
                "Seasonal reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Seasonal planting and care reminders"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(listOf(waterChannel, seasonalChannel))
        }
    }
}