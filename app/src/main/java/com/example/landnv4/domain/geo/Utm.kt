package com.example.landnv4.domain.geo

data class Utm(
    val zone: Int,
    val hemisphereNorth: Boolean,
    val easting: Double,
    val northing: Double
)
