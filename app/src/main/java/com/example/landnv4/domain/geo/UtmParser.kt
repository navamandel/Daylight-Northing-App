package com.example.landnv4.domain.geo

object UtmParser {


    fun parseUtm(rawE: String, rawN: String, zone: Int = 36, hemisphereNorth: Boolean = true): Utm {

        val easting = validateEasting(rawE)
        if(easting !in 100_000.0..900_000.0) throw IllegalArgumentException(
            "UTM Easting must be all digits in the range ~100000..900000 and in the format EEEEEE(.EE)"
        )

        val northing = validateNorthing(rawN)
        if(northing !in 0.0..10_000_000.0) throw IllegalArgumentException(
            "UTM Northing must be all digits in the range 0..10000000 and in the format NNNNNNN(.NN)"
        )

        if(zone !in 0..60) throw IllegalArgumentException("UTM zone must be in the range 1..60")

        return Utm(easting, northing, zone, hemisphereNorth)
    }

    fun validateEasting(e: String): Double {

        val sE = e.trim().replace(" ", "")
        val estParts = sE.split(".")

        if (estParts[0].length != 6 || !(estParts[0].all { it.isDigit() })) return -1.0
        if (estParts.size == 2 && !(estParts[1].all { it.isDigit() })) return -1.0

        return e.toDouble().roundTo2()
    }

    fun validateNorthing(n: String): Double {
        val sN = n.trim().replace(" ", "")
        val nthParts = sN.split(".")

        if (nthParts[0].length != 7 || !(nthParts[0].all { it.isDigit() })) return -1.0
        if (nthParts.size == 2 && !(nthParts[1].all { it.isDigit() })) return -1.0

        return sN.toDouble().roundTo2()
    }

    fun Double.roundTo2(): Double = kotlin.math.round(this * 100.0) / 100.0

    fun Utm.utmToString(): String =
        "${this.easting},${this.northing},${this.zone},${this.hemisphereNorth}"

    fun String.toUtm(): Utm {
        val parts = this.split(",")
        return parseUtm(parts[0], parts[1], parts[2].toInt(), parts[3].toBoolean())
    }


}
