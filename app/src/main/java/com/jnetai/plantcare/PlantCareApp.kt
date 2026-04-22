package com.jnetai.plantcare

import android.app.Application
import androidx.room.Room
import com.jnetai.plantcare.data.PlantCareDatabase

class PlantCareApp : Application() {
    val database: PlantCareDatabase by lazy {
        Room.databaseBuilder(
            this,
            PlantCareDatabase::class.java,
            "plantcare.db"
        ).build()
    }
}