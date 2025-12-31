package com.example.landnv4.domain.geo

import kotlin.math.*

data class Wgs84(val latDeg: Double, val lonDeg: Double, val altMeters: Double = 0.0)
data class Ecef(val x: Double, val y: Double, val z: Double)

object GeoCore {
    // WGS84 ellipsoid constants
    private const val A = 6378137.0
    private const val F = 1.0 / 298.257223563
    private const val E2 = F * (2 - F)

    fun wgs84ToEcef(p: Wgs84): Ecef {
        val lat = Math.toRadians(p.latDeg)
        val lon = Math.toRadians(p.lonDeg)
        val sinLat = sin(lat)
        val cosLat = cos(lat)
        val sinLon = sin(lon)
        val cosLon = cos(lon)

        val n = A / sqrt(1 - E2 * sinLat * sinLat)

        val x = (n + p.altMeters) * cosLat * cosLon
        val y = (n + p.altMeters) * cosLat * sinLon
        val z = (n * (1 - E2) + p.altMeters) * sinLat
        return Ecef(x, y, z)
    }

    fun ecefToWgs84(e: Ecef): Wgs84 {
        // Bowring-like iterative method (good enough for alt≈0 use)
        val x = e.x; val y = e.y; val z = e.z
        val lon = atan2(y, x)

        val p = sqrt(x*x + y*y)
        var lat = atan2(z, p * (1 - E2))
        repeat(5) {
            val sinLat = sin(lat)
            val n = A / sqrt(1 - E2 * sinLat * sinLat)
            lat = atan2(z + E2 * n * sinLat, p)
        }

        val sinLat = sin(lat)
        val n = A / sqrt(1 - E2 * sinLat * sinLat)
        val alt = p / cos(lat) - n

        return Wgs84(Math.toDegrees(lat), Math.toDegrees(lon), alt)
    }

    data class Dms(val d: Int, val m: Int, val s: Double, val hemi: Char)

    fun decimalToDms(valueDeg: Double, isLat: Boolean): Dms {
        val hemi = when {
            isLat && valueDeg >= 0 -> 'N'
            isLat -> 'S'
            !isLat && valueDeg >= 0 -> 'E'
            else -> 'W'
        }
        val v = abs(valueDeg)
        val d = v.toInt()
        val mFull = (v - d) * 60.0
        val m = mFull.toInt()
        val s = (mFull - m) * 60.0
        return Dms(d, m, s, hemi)
    }

    fun dmsToDecimal(d: Int, m: Int, s: Double, hemi: Char): Double {
        val sign = if (hemi.uppercaseChar() == 'S' || hemi.uppercaseChar() == 'W') -1 else 1
        return sign * (abs(d).toDouble() + m / 60.0 + s / 3600.0)
    }
}
