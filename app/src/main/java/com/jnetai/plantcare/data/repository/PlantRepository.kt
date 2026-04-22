package com.jnetai.plantcare.data.repository

import com.jnetai.plantcare.data.dao.PlantDao
import com.jnetai.plantcare.data.entity.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlantRepository(private val plantDao: PlantDao) {

    fun getAllPlants(): Flow<List<Plant>> = plantDao.getAllPlants()

    suspend fun getPlantById(id: Long): Plant? = withContext(Dispatchers.IO) {
        plantDao.getPlantById(id)
    }

    suspend fun insert(plant: Plant): Long = withContext(Dispatchers.IO) {
        plantDao.insert(plant)
    }

    suspend fun update(plant: Plant) = withContext(Dispatchers.IO) {
        plantDao.update(plant)
    }

    suspend fun delete(plant: Plant) = withContext(Dispatchers.IO) {
        plantDao.delete(plant)
    }

    suspend fun getAllPlantsList(): List<Plant> = withContext(Dispatchers.IO) {
        plantDao.getAllPlantsList()
    }
}