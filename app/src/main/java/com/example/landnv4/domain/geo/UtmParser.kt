package com.example.landnv4.domain.geo

object UtmParser {
    /**
     * Your "12-digit UTM" assumed as 6 digits easting + 6 digits northing (meters).
     * Example: "123456789012" => easting=123456, northing=789012
     */
    fun parse12Digits(raw: String, zone: Int, hemisphereNorth: Boolean): Utm {
        val s = raw.trim().replace(" ", "")
        require(s.length == 12 && s.all { it.isDigit() }) { "UTM must be exactly 12 digits" }

        val easting = s.substring(0, 6).toDouble()
        val northing = s.substring(6, 12).toDouble()

        return Utm(
            zone = zone,
            hemisphereNorth = hemisphereNorth,
            easting = easting,
            northing = northing
        )
    }
}
