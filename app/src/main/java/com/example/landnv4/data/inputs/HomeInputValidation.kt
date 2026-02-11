package com.example.landnv4.data.inputs

import com.example.landnv4.domain.geo.Utm
import com.example.landnv4.domain.geo.UtmParser.toUtm
import com.example.landnv4.domain.geo.converters.UtmConverter
import java.time.LocalDate
import java.time.format.DateTimeParseException

object HomeInputValidation {

    fun validateDateIso(s: String) {
        val d = s.trim()
            .replace(" ", "-")
            .replace(":", "-")
            .replace(",", "-")
        try {
            LocalDate.parse(d)
        } catch (e: Exception) {
            throw e
        }
    }

    fun validateTimeHundredth(s: String) {
        val t = s.trim()
            .replace(" ", ":")
            .replace("-", ":")
            .replace(",", ":")
        // HH:mm:ss.SS
        val m = Regex("""^(\d{2}):(\d{2}):(\d{2})\.(\d{2})$""").matchEntire(t)
            ?: throw java.lang.IllegalArgumentException("Time must be HH:MM:SS.ss")

        val (hh, mm, ss, hs) = m.destructured
        val H = hh.toInt()
        val M = mm.toInt()
        val S = ss.toInt()
        val HS = hs.toInt()

        if (H !in 0..23) throw IllegalArgumentException("Hours must be 00-23")
        if (M !in 0..59) throw IllegalArgumentException("Minutes must be 00-59")
        if (S !in 0..59) throw IllegalArgumentException("Seconds must be 00-59")
        if (HS !in 0..99) throw IllegalArgumentException("Hundredths must be 00-99")

    }

    fun validateUtm13(u: Utm): String? {
        /*val v = s.trim().replace(" ", "")
        if (v.length != 13 || !v.all { it.isDigit() }) return "UTM must be exactly 13 digits"
        if (v.all { it == '0' }) return "UTM cannot be all zeros"

        val easting = v.substring(0, 6).toInt()
        val northing = v.substring(6, 13).toInt()

        // Very loose sanity bounds (won't reject real values but catches typos)
        if (easting !in 100000..9000000) return "UTM easting looks invalid"
        if (northing !in 0..10000000) return "UTM northing looks invalid"*/

        val errors = UtmConverter.validate(u)
        return if (!errors.isEmpty()) errors.joinToString("; ") else null
    }
}