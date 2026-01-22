package com.example.landnv4.domain.geo

// import com.example.landnv4.domain.geo.GeoFormats.Ecef
// import com.example.landnv4.domain.geo.GeoFormats.Mercator
import com.example.landnv4.domain.geo.converters.Dms
import com.example.landnv4.domain.geo.converters.Ecef
import kotlin.math.*

data class Wgs84(val latDeg: Double, val lonDeg: Double, var altMeters: Double = 0.0)
data class Ecef(val x: Double, val y: Double, val z: Double)
// data class Dms(val d: Int, val m: Int, val s: Double, val hemi: Char)
data class Mercator(val x: Double, val y: Double)

object GeoCore {
    // WGS84 ellipsoid constants
    const val A = 6378137.0
    private const val F = 1.0 / 298.257223563
    const val E2 = F * (2 - F)

    public fun wgs84ToEcef(p: Wgs84): Ecef {
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

    public fun ecefToWgs84(e: Ecef): Wgs84 {
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



    /*public fun decimalToDms(valueDeg: Double, isLat: Boolean): Dms {
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
    }*/

    public fun dmsToDecimal(d: Int, m: Int, s: Double, hemi: Char): Double {
        val sign = if (hemi.uppercaseChar() == 'S' || hemi.uppercaseChar() == 'W') -1 else 1
        return sign * (abs(d).toDouble() + m / 60.0 + s / 3600.0)
    }

    public fun latLonToWebMercator(latDeg: Double, lonDeg: Double): Mercator {
        val R = 6378137.0
        val x = Math.toRadians(lonDeg) * R
        val lat = latDeg.coerceIn(-85.05112878, 85.05112878)
        val y = R * ln(tan(Math.PI / 4 + Math.toRadians(lat) / 2))
        return Mercator(x, y)
    }

    public fun latLonToEcef(latDeg: Double, lonDeg: Double, alt: Double = 0.0): Ecef {
        val a = 6378137.0
        val f = 1.0 / 298.257223563
        val e2 = f * (2 - f)

        val lat = Math.toRadians(latDeg)
        val lon = Math.toRadians(lonDeg)

        val sinLat = sin(lat)
        val cosLat = cos(lat)
        val sinLon = sin(lon)
        val cosLon = cos(lon)

        val N = a / sqrt(1 - e2 * sinLat * sinLat)

        val x = (N + alt) * cosLat * cosLon
        val y = (N + alt) * cosLat * sinLon
        val z = (N * (1 - e2) + alt) * sinLat
        return Ecef(x, y, z)
    }

    public fun webMercatorToLatLon(x: Double, y: Double): Wgs84 {
        val R = 6378137.0
        val lon = Math.toDegrees(x / R)
        val lat = Math.toDegrees(2 * Math.atan(Math.exp(y / R)) - Math.PI / 2)
        return Wgs84(lon, lat)
    }
}
