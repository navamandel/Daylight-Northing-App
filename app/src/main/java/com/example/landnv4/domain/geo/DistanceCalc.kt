package com.example.landnv4.domain.geo

import kotlin.math.*

object DistanceCalc {
    private const val R = 6371008.8 // mean Earth radius meters

    data class Result(
        val distanceMeters: Double,
        val initialBearingDeg: Double,
        val distance3dMeters: Double
    )

    fun haversine(p1: Wgs84, p2: Wgs84): Result {
        val lat1 = Math.toRadians(p1.latDeg)
        val lon1 = Math.toRadians(p1.lonDeg)
        val lat2 = Math.toRadians(p2.latDeg)
        val lon2 = Math.toRadians(p2.lonDeg)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        val d = R * c
        val surface = R * c

        // bearing
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var brng = Math.toDegrees(atan2(y, x))
        brng = (brng + 360.0) % 360.0

        val dz = p2.altMeters - p1.altMeters
        val d3 = sqrt(surface * surface + dz * dz)

        return Result(d, brng, d3)
    }

    /** Straight-line 3D distance using altitude difference + surface distance */
    fun distance3D(p1: Wgs84, p2: Wgs84): Double {
        val surface = haversine(p1, p2).distanceMeters
        val dz = p2.altMeters - p1.altMeters
        return sqrt(surface * surface + dz * dz)
    }
}
