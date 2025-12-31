package com.example.landnv4

import java.time.LocalDate

object HomeInputValidation {

    fun validateDateIso(s: String): String? {
        return try {
            LocalDate.parse(s.trim())
            null
        } catch (e: Exception) {
            "Date must be YYYY-MM-DD"
        }
    }

    fun validateTimeHundredth(s: String): String? {
        val t = s.trim()
        // HH:mm:ss.SS
        val m = Regex("""^(\d{2}):(\d{2}):(\d{2})\.(\d{2})$""").matchEntire(t)
            ?: return "Time must be HH:mm:ss.SS"

        val (hh, mm, ss, hs) = m.destructured
        val H = hh.toInt()
        val M = mm.toInt()
        val S = ss.toInt()
        val HS = hs.toInt()

        if (H !in 0..23) return "Hours must be 00-23"
        if (M !in 0..59) return "Minutes must be 00-59"
        if (S !in 0..59) return "Seconds must be 00-59"
        if (HS !in 0..99) return "Hundredths must be 00-99"
        return null
    }

    fun validateUtm14(s: String): String? {
        val v = s.trim().replace(" ", "")
        if (v.length != 14 || !v.all { it.isDigit() }) return "UTM must be exactly 14 digits"
        if (v.all { it == '0' }) return "UTM cannot be all zeros"

        val easting = v.substring(0, 7).toInt()
        val northing = v.substring(7, 14).toInt()

        // Very loose sanity bounds (won't reject real values but catches typos)
        if (easting !in 100000..9000000) return "UTM easting looks invalid"
        if (northing !in 0..10000000) return "UTM northing looks invalid"

        return null
    }
}
