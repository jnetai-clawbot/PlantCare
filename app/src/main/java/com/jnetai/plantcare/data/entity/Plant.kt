package com.jnetai.plantcare.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val species: String = "",
    val location: String = "",
    val photoPath: String = "",
    val datePlanted: Long = System.currentTimeMillis(),
    val wateringIntervalDays: Int = 7,
    val lastWatered: Long = System.currentTimeMillis(),
    val sunlight: String = "Indirect light",
    val notes: String = ""
)