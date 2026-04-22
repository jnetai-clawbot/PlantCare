package com.jnetai.plantcare.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jnetai.plantcare.data.dao.PlantDao
import com.jnetai.plantcare.data.dao.HealthLogDao
import com.jnetai.plantcare.data.dao.WishlistDao
import com.jnetai.plantcare.data.entity.Plant
import com.jnetai.plantcare.data.entity.HealthLogEntry
import com.jnetai.plantcare.data.entity.WishlistItem

@Database(
    entities = [Plant::class, HealthLogEntry::class, WishlistItem::class],
    version = 1,
    exportSchema = false
)
abstract class PlantCareDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun healthLogDao(): HealthLogDao
    abstract fun wishlistDao(): WishlistDao
}