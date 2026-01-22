package com.example.landnv4.ui.databank

import com.example.landnv4.data.db.infobank.HeightType
import com.example.landnv4.domain.geo.Utm


data class PointItem(
    val id: Long,
    val utm: Utm,
    val location: String,
    val height: Double,
    val heightType: HeightType,
    val name: String
)

data class PointItems(
    val anchoring_points: List<PointItem>,
    val northing_points: List<PointItem>,
    val validating_points: List<PointItem>,
    val targets: List<PointItem>
)

