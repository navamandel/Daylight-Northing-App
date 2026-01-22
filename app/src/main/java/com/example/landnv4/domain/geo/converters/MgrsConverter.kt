package com.example.landnv4.domain.geo.converters

import mil.nga.mgrs.MGRS

object MgrsConverter {

    private val mgrsRegex = Regex(
        """^\s*(\d{1,2})([C-HJ-NP-X])\s*([A-HJ-NP-Z]{2})\s*(\d{2,10})\s*$""",
        RegexOption.IGNORE_CASE
    )

    fun validate(p: String): List<String> {
        val errors = mutableListOf<String>()
        val s = p.trim().replace("\\s+".toRegex(), "")
        val m = mgrsRegex.matchEntire(s)
        if (m == null) {
            errors += "MGRS must look like: <zone><band><2 letters><even digits>, e.g. 33UXP050044"
        } else {
            val zone = m.groupValues[1].toInt()
            val digits = m.groupValues[4]
            if (zone !in 1..60) errors += "MGRS zone must be 1..60"
            if (digits.length % 2 != 0) errors += "MGRS numeric precision must have even number of digits"
            if (digits.length !in 2..10) errors += "MGRS numeric part length must be 2..10"
        }
        return errors
    }

    /*fun parse(text: String, altMeters: String? = null): String {
        val p = String(text.trim())
        val errs = validate(p)
        require(errs.isEmpty()) { errs.joinToString("; ") }
        return p
    }*/
//         val base = "UTM: ${p.zone}$hemi E=%.0f N=%.0f".format(p.easting, p.northing)
    fun format(p: String): String = p.trim().replace("\\s+".toRegex(), "")

    fun latLonToMgrs(latDeg: Double, lonDeg: Double, accuracy: Int = 5): String {
        require(accuracy in 0..5) { "MGRS accuracy must be 0..5" }

        // IMPORTANT: from(longitude, latitude)
        val mgrs = MGRS.from(lonDeg, latDeg)  // order matters :contentReference[oaicite:1]{index=1}
        return mgrs.coordinate(accuracy)      // not toString(accuracy) :contentReference[oaicite:2]{index=2}
    }

    fun mgrsToLatLon(mgrsString: String): LatLon {
        val mgrs = MGRS.parse(mgrsString.trim()) // :contentReference[oaicite:3]{index=3}
        val point = mgrs.toPoint()
        val ll = LatLon(point.getLatitude(), point.getLongitude())

        return ll // :contentReference[oaicite:4]{index=4}
    }

}