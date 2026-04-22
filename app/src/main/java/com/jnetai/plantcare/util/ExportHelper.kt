package com.jnetai.plantcare.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jnetai.plantcare.PlantCareApp
import com.jnetai.plantcare.data.entity.HealthLogEntry
import com.jnetai.plantcare.data.entity.Plant
import com.jnetai.plantcare.data.entity.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ExportData(
    val plants: List<PlantExport>,
    val wishlist: List<WishlistItemExport>,
    val exportDate: String
)

data class PlantExport(
    val name: String,
    val species: String,
    val location: String,
    val datePlanted: String,
    val wateringIntervalDays: Int,
    val lastWatered: String,
    val sunlight: String,
    val notes: String,
    val healthLog: List<HealthLogExport>
)

data class HealthLogExport(
    val date: String,
    val note: String
)

data class WishlistItemExport(
    val name: String,
    val notes: String
)

object ExportHelper {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToJson(context: Context): String = withContext(Dispatchers.IO) {
        val db = (context.applicationContext as PlantCareApp).database
        val plants = db.plantDao().getAllPlantsList()
        val wishlist = db.wishlistDao().getAllItemsList()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)

        val plantExports = plants.map { plant ->
            val healthEntries = db.healthLogDao().getEntriesForPlantList(plant.id)
            PlantExport(
                name = plant.name,
                species = plant.species,
                location = plant.location,
                datePlanted = sdf.format(java.util.Date(plant.datePlanted)),
                wateringIntervalDays = plant.wateringIntervalDays,
                lastWatered = sdf.format(java.util.Date(plant.lastWatered)),
                sunlight = plant.sunlight,
                notes = plant.notes,
                healthLog = healthEntries.map { entry ->
                    HealthLogExport(
                        date = dateTimeFormat.format(java.util.Date(entry.date)),
                        note = entry.note
                    )
                }
            )
        }

        val wishlistExports = wishlist.map { item ->
            WishlistItemExport(name = item.name, notes = item.notes)
        }

        val data = ExportData(
            plants = plantExports,
            wishlist = wishlistExports,
            exportDate = dateTimeFormat.format(java.util.Date())
        )

        val json = gson.toJson(data)

        // Save to file
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        exportDir.mkdirs()
        val exportFile = File(exportDir, "plantcare_export_${System.currentTimeMillis()}.json")
        exportFile.writeText(json)

        exportFile.absolutePath
    }
}