package com.example.landnv4.data.db.infobank

import androidx.room.TypeConverter

enum class HeightType { METERS, FEET }

class Converters {
    @androidx.room.TypeConverter
    fun toHeightType(value: String): HeightType = HeightType.valueOf(value)

    @androidx.room.TypeConverter
    fun fromHeightType(type: HeightType): String = type.name
}

object HeightConverters {

    @TypeConverter
    fun toHeightType(value: String): HeightType =
        HeightType.valueOf(value)

    @TypeConverter
    fun fromHeightType(type: HeightType): String =
        type.name
}

