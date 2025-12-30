package com.example.landnv4.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stars")
data class StarEntity(
    @PrimaryKey val id: Int,          // CSV "id"
    val hip: Int?,                    // CSV "hip"
    val proper: String?,              // CSV "proper"
    val ra: Double,                   // CSV "ra" (degrees)
    val dec: Double,                  // CSV "dec" (degrees)
    val mag: Double?                  // CSV "mag" (optional, useful for sorting later)
)
