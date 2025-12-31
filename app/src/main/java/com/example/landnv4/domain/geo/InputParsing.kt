package com.example.landnv4.domain.geo

object InputParsing {

    /** Splits by comma or whitespace; keeps numbers and tokens */
    fun tokens(s: String): List<String> =
        s.replace("->", " -> ")
            .replace(",", " ")
            .split(" ")
            .map { it.trim() }
            .filter { it.isNotBlank() }

    fun parseLatLonAlt(input: String): Wgs84 {
        // "lat lon [alt]" or "lat,lon[,alt]"
        val t = tokens(input)
        if (t.size < 2) throw IllegalArgumentException("Lat,Lon format: lat,lon or lat lon")
        val lat = t[0].toDouble()
        val lon = t[1].toDouble()
        val alt = if (t.size >= 3) t[2].toDouble() else 0.0
        return Wgs84(lat, lon, alt)
    }

    fun parseXYAlt(input: String): Triple<Double, Double, Double> {
        // "x y [alt]" or "x,y[,alt]"
        val t = tokens(input)
        if (t.size < 2) throw IllegalArgumentException("Format: x,y or x y (alt optional)")
        val x = t[0].toDouble()
        val y = t[1].toDouble()
        val alt = if (t.size >= 3) t[2].toDouble() else 0.0
        return Triple(x, y, alt)
    }
}
