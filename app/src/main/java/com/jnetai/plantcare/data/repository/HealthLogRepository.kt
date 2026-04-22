package com.jnetai.plantcare.data.repository

import com.jnetai.plantcare.data.dao.HealthLogDao
import com.jnetai.plantcare.data.entity.HealthLogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HealthLogRepository(private val healthLogDao: HealthLogDao) {

    fun getEntriesForPlant(plantId: Long): Flow<List<HealthLogEntry>> =
        healthLogDao.getEntriesForPlant(plantId)

    suspend fun insert(entry: HealthLogEntry): Long = withContext(Dispatchers.IO) {
        healthLogDao.insert(entry)
    }

    suspend fun delete(entry: HealthLogEntry) = withContext(Dispatchers.IO) {
        healthLogDao.delete(entry)
    }

    suspend fun getEntriesForPlantList(plantId: Long): List<HealthLogEntry> =
        withContext(Dispatchers.IO) {
            healthLogDao.getEntriesForPlantList(plantId)
        }
}