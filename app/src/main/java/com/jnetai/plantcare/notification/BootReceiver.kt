package com.jnetai.plantcare.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jnetai.plantcare.PlantCareApp
import com.jnetai.plantcare.data.entity.Plant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.createChannels(context)
            rescheduleAllReminders(context)
        }
    }

    private fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = (context.applicationContext as PlantCareApp).database
            val plants = db.plantDao().getAllPlantsList()
            for (plant in plants) {
                if (plant.wateringIntervalDays > 0) {
                    WaterReminderReceiver.schedule(
                        context,
                        plant.id,
                        plant.name,
                        plant.wateringIntervalDays
                    )
                }
            }
        }
    }
}