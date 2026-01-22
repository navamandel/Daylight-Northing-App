package com.example.landnv4.data.db.infobank

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.landnv4.domain.geo.Utm

@Entity(tableName = "anchoring_points")
data class AnchoringPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @Embedded(prefix = "utm_")
    val utm: Utm,

    val location: String,
    val height: Double,
    val heightType: HeightType,
    val name: String
)

@Entity(tableName = "northing_points")
data class NorthingPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @Embedded(prefix = "utm_")
    val utm: Utm,

    val location: String,
    val height: Double,
    val heightType: HeightType,
    val name: String
)

@Entity(tableName = "validating_points")
data class ValidatingPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @Embedded(prefix = "utm_")
    val utm: Utm,

    val location: String,
    val height: Double,
    val heightType: HeightType,
    val name: String
)

@Entity(tableName = "targets")
data class TargetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @Embedded(prefix = "utm_")
    val utm: Utm,

    val location: String,
    val height: Double,
    val heightType: HeightType,
    val name: String
)
