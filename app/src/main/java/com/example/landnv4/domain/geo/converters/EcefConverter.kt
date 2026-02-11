package com.example.landnv4.domain.geo.converters

import com.example.landnv4.domain.geo.GeoCore.A
import com.example.landnv4.domain.geo.GeoCore.E2
import com.example.landnv4.domain.geo.InputParsing.isFiniteReal
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Ecef(val x: Double, val y: Double, val z: Double)

object EcefConverter {

    fun validate(p: Ecef) {
        if (!p.x.isFiniteReal()) throw IllegalArgumentException("ECEF X must be a real number")
        if (!p.y.isFiniteReal()) throw IllegalArgumentException("ECEF Y must be a real number")
        if (!p.z.isFiniteReal()) throw IllegalArgumentException("ECEF Z must be a real number")

        // Optional: loose magnitude sanity check (Earth radius ~6.37e6 m)
        val r2 = p.x*p.x + p.y*p.y + p.z*p.z
        if (!r2.isFiniteReal() || r2 <= 0.0) throw IllegalArgumentException("ECEF vector must be non-zero")

    }

    fun parse(inputX: String, inputY: String, inputZ: String): Ecef {
        /*val xx = x.trim().toDoubleOrNull() ?: error("Invalid X")
        val yy = y.trim().toDoubleOrNull() ?: error("Invalid Y")
        val zz = z.trim().toDoubleOrNull() ?: error("Invalid Z")
        val p = Ecef(xx, yy, zz)
        val errs = validate(p)
        require(errs.isEmpty()) { errs.joinToString("; ") }
        return p*/

        val p = Ecef(inputX.toDouble(), inputY.toDouble(), inputZ.toDouble())
        try {
            validate(p)
            return p
        } catch (e: Exception) {
            throw e
        }
    }

    fun format(p: Ecef): String =
        "X=%.3f Y=%.3f Z=%.3f".format(p.x, p.y, p.z)


    fun ecefToLatLon(e: Ecef): LatLon {
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

        return LatLon(Math.toDegrees(lat), Math.toDegrees(lon), alt)
    }
}