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
    fun toHeightTypeN(value: String?): HeightType? =
        if (value == null) null else HeightType.valueOf(value)

    @TypeConverter
    fun HeightType?.fromHeightType(): String? =
        this?.name

    fun HeightType.toPrettyString(): String =
        this.name.lowercase().replaceFirstChar { it.uppercase() }

}

