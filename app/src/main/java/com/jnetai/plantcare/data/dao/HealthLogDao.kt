package com.jnetai.plantcare.data.dao

import androidx.room.*
import com.jnetai.plantcare.data.entity.HealthLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthLogDao {
    @Query("SELECT * FROM health_log WHERE plantId = :plantId ORDER BY date DESC")
    fun getEntriesForPlant(plantId: Long): Flow<List<HealthLogEntry>>

    @Insert
    suspend fun insert(entry: HealthLogEntry): Long

    @Delete
    suspend fun delete(entry: HealthLogEntry)

    @Query("SELECT * FROM health_log WHERE plantId = :plantId ORDER BY date DESC")
    suspend fun getEntriesForPlantList(plantId: Long): List<HealthLogEntry>
}