package com.example.landnv4.domain.geo.converters

import com.example.landnv4.domain.geo.GeoCore
import com.example.landnv4.domain.geo.GeoFormats
import com.example.landnv4.domain.geo.InputParsing.isFiniteReal
import com.example.landnv4.domain.geo.InputParsing.parseNullableDouble
import kotlin.math.abs

data class Dms(
    val latDeg: Int,
    val latMin: Int,
    val latSec: Double,
    val latHem: Char,   // 'N' or 'S'

    val lonDeg: Int,
    val lonMin: Int,
    val lonSec: Double,
    val lonHem: Char    // 'E' or 'W'

)

object DmsConverter {

    fun validate(p: Dms) {

        fun checkLat() {
            if (p.latHem !in charArrayOf('N','S')) throw IllegalArgumentException("Latitude hemisphere must be N or S")
            if (p.latDeg !in 0..90) throw IllegalArgumentException("Latitude degrees must be 0..90")
            if (p.latMin !in 0..59) throw IllegalArgumentException("Latitude minutes must be 0..59")
            if (!p.latSec.isFiniteReal() || p.latSec !in 0.0..<60.0) throw IllegalArgumentException("Latitude seconds must be 0..60")
            if (p.latDeg == 90 && (p.latMin != 0 || p.latSec != 0.0)) throw IllegalArgumentException("Latitude 90° must have 0'0\"")
        }

        fun checkLon() {
            if (p.lonHem !in charArrayOf('E','W')) throw IllegalArgumentException("Longitude hemisphere must be E or W")
            if (p.lonDeg !in 0..180) throw IllegalArgumentException("Longitude degrees must be 0..180")
            if (p.lonMin !in 0..59) throw IllegalArgumentException("Longitude minutes must be 0..59")
            if (!p.lonSec.isFiniteReal() || p.lonSec !in 0.0..<60.0) throw IllegalArgumentException("Longitude seconds must be 0..60")
            if (p.lonDeg == 180 && (p.lonMin != 0 || p.lonSec != 0.0)) throw IllegalArgumentException("Longitude 180° must have 0'0\"")
        }

        checkLat()
        checkLon()

    }

    /** Parse a compact string like: 31°46'36.1"N 35°14'04.2"E */
    fun parse(input: String): Dms {
        /*val t = text.trim()
        val regex = Regex(
            """(\d{1,2})\D+(\d{1,2})\D+(\d+(?:\.\d+)?)\s*([NS])\s+(\d{1,3})\D+(\d{1,2})\D+(\d+(?:\.\d+)?)\s*([EW])""",
            RegexOption.IGNORE_CASE
        )
        val m = regex.find(t) ?: error("Invalid DMS format")
        val (aD,aM,aS,aH, oD,oM,oS,oH) = m.destructured
        val p = Dms(
            latDeg = aD.toInt(), latMin = aM.toInt(), latSec = aS.toDouble(), latHem = aH.uppercase()[0],
            lonDeg = oD.toInt(), lonMin = oM.toInt(), lonSec = oS.toDouble(), lonHem = oH.uppercase()[0],
            altMeters = parseNullableDouble(altMeters)
        )
        val errs = validate(p)
        require(errs.isEmpty()) { errs.joinToString("; ") }
        return p*/

        val parts = input.replace(",", " ").split(" ").filter { it.isNotBlank() }
        if (parts.size < 8) throw IllegalArgumentException("DMS format: 31 46 41 N, 35 14 06 E")

        val p = Dms(
            latDeg = parts[0].toInt(), latMin = parts[1].toInt(), latSec = parts[2].toDouble(), latHem = parts[3][0].toChar(),
            lonDeg = parts[4].toInt(), lonMin = parts[5].toInt(), lonSec = parts[6].toDouble(), lonHem = parts[7][0].toChar()
        )
        try {
            validate(p)
            return p
        } catch (e: Exception) {
            throw e
        }
    }

    fun format(p: Dms): String = "%d°%02d'%06.3f\"%c %d°%02d'%06.3f\"%c".format(
            p.latDeg, p.latMin, p.latSec, p.latHem,
            p.lonDeg, p.lonMin, p.lonSec, p.lonHem
        )



    fun dmsToLatLon(p: Dms): LatLon {
        fun fromDms(deg: Int, min: Int, sec: Double, hem: Char): Double {
            var value = deg + min / 60.0 + sec / 3600.0
            if (hem == 'S' || hem == 'W') value = -value
            return value
        }

        val lat = fromDms(p.latDeg, p.latMin, p.latSec, p.latHem)
        val lon = fromDms(p.lonDeg, p.lonMin, p.lonSec, p.lonHem)

        return LatLon(lat, lon)
    }

}
