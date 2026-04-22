package com.jnetai.plantcare.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PhotoHelper {
    private const val DIR_PHOTOS = "plant_photos"
    private const val DIR_HEALTH = "health_photos"
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun savePlantPhoto(context: Context, sourceUri: Uri, plantId: Long): String {
        val dir = File(context.filesDir, DIR_PHOTOS)
        dir.mkdirs()
        val file = File(dir, "plant_${plantId}_${dateFormat.format(Date())}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun saveHealthPhoto(context: Context, sourceUri: Uri, plantId: Long, entryId: Long): String {
        val dir = File(context.filesDir, DIR_HEALTH)
        dir.mkdirs()
        val file = File(dir, "health_${plantId}_${entryId}_${dateFormat.format(Date())}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun loadPhoto(path: String): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        return if (file.exists()) {
            BitmapFactory.decodeFile(path)
        } else null
    }

    fun deletePhoto(path: String) {
        if (path.isNotBlank()) {
            File(path).takeIf { it.exists() }?.delete()
        }
    }

    fun copyPhotoForNewPlant(context: Context, tempPath: String, newPlantId: Long): String {
        if (tempPath.isBlank()) return ""
        val src = File(tempPath)
        if (!src.exists()) return ""
        val dir = File(context.filesDir, DIR_PHOTOS)
        dir.mkdirs()
        val dest = File(dir, "plant_${newPlantId}_${dateFormat.format(Date())}.jpg")
        src.copyTo(dest, overwrite = true)
        src.delete()
        return dest.absolutePath
    }
}