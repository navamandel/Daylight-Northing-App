package com.example.landnv4.domain.geo

import kotlin.math.*

object GeoFormats {

    data class Dms(val d: Int, val m: Int, val s: Double, val hemi: Char)

    fun decimalToDms(value: Double, isLat: Boolean): Dms {
        val hemi = when {
            isLat && value >= 0 -> 'N'
            isLat -> 'S'
            !isLat && value >= 0 -> 'E'
            else -> 'W'
        }
        val v = abs(value)
        val d = v.toInt()
        val mFull = (v - d) * 60.0
        val m = mFull.toInt()
        val s = (mFull - m) * 60.0
        return Dms(d, m, s, hemi)
    }

    fun formatDms(dms: Dms): String =
        "%d°%02d'%05.2f\"%c".format(dms.d, dms.m, dms.s, dms.hemi)

    fun formatDdm(value: Double, isLat: Boolean): String {
        val hemi = when {
            isLat && value >= 0 -> 'N'
            isLat -> 'S'
            !isLat && value >= 0 -> 'E'
            else -> 'W'
        }
        val v = abs(value)
        val d = v.toInt()
        val minutes = (v - d) * 60.0
        return "%d°%07.4f'%c".format(d, minutes, hemi)
    }

    // Web Mercator (EPSG:3857) from lat/lon degrees
    data class Mercator(val x: Double, val y: Double)

    fun latLonToWebMercator(latDeg: Double, lonDeg: Double): Mercator {
        val R = 6378137.0
        val x = Math.toRadians(lonDeg) * R
        val lat = latDeg.coerceIn(-85.05112878, 85.05112878)
        val y = R * ln(tan(Math.PI / 4 + Math.toRadians(lat) / 2))
        return Mercator(x, y)
    }

    // ECEF (WGS84) (alt assumed 0)
    data class Ecef(val x: Double, val y: Double, val z: Double)

    fun latLonToEcef(latDeg: Double, lonDeg: Double, alt: Double = 0.0): Ecef {
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
}
