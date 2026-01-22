package com.example.landnv4.domain.geo

data class Utm(
    val easting: Double,
    val northing: Double,
    val zone: Int,
    val hemisphereNorth: Boolean
)

