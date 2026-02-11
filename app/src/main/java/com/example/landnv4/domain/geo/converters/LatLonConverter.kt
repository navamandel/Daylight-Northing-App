package com.example.landnv4.domain.geo.converters


import com.example.landnv4.domain.geo.GeoCore
import com.example.landnv4.domain.geo.InputParsing.isFiniteReal
import com.example.landnv4.domain.geo.InputParsing.parseNullableDouble
// import com.example.landnv4.domain.geo.Itm
// import com.example.landnv4.domain.geo.converters.ItmConverter
// import com.example.landnv4.domain.geo.Mercator
import com.example.landnv4.domain.geo.Utm
import mil.nga.mgrs.MGRS
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class LatLon(val lat: Double, val lon: Double, var alt: Double = 0.0)

object LatLonConverter {
    private val crsFactory = CRSFactory()
    private val ctFactory = CoordinateTransformFactory()
    private val itmCrs = crsFactory.createFromParameters(
        "EPSG:2039",
        "+proj=tmerc +lat_0=31.7343936111111 +lon_0=35.2045169444444 " +
                "+k=1.0000067 +x_0=219529.584 +y_0=626907.39 " +
                "+ellps=GRS80 " +
                "+towgs84=23.772,17.49,17.859,-0.3132,-1.85274,1.67299,-5.4262 " +
                "+units=m +no_defs"
    )

    private val wgs84 = crsFactory.createFromParameters(
        "WGS84",
        "+proj=longlat +datum=WGS84 +no_defs"
    )

    private val toItmTransform = ctFactory.createTransform(wgs84, itmCrs)

    fun validate(p: LatLon): List<String> {
        val errors = mutableListOf<String>()
        if (!p.lat.isFiniteReal() || p.lat !in -90.0..90.0) errors += "Latitude must be between -90 and 90"
        if (!p.lon.isFiniteReal() || p.lon !in -180.0..180.0) errors += "Longitude must be between -180 and 180"
        //if (p.alt != null && !p.alt.isFiniteReal()) errors += "Altitude must be a real number"
        return errors
    }

    fun parse(inputE: String, inputN: String): LatLon {
        /*val la = lat.trim().toDoubleOrNull() ?: error("Invalid latitude")
        val lo = lon.trim().toDoubleOrNull() ?: error("Invalid longitude")
        val alt = if (altMeters != null) altMeters.toDouble() else 0.0
        val p = LatLon(la, lo, alt)
        val errs = validate(p)
        require(errs.isEmpty()) { errs.joinToString("; ") }
        return p*/

        val lat = inputE.toDouble()
        require(lat.isFiniteReal() || lat in -90.0..90.0) { "Latitude must be between -90 and 90" }

        val lon = inputN.toDouble()
        require(lon.isFiniteReal() || lon in -180.0..180.0) {"Longitude must be between -180 and 180"}

        val p = LatLon(lat, lon)

        return p
    }

    fun format(p: LatLon): String = "lat=%.6f, lon=%.6f".format(p.lat, p.lon)


    public fun latLonToEcef(p: LatLon): Ecef {
        val lat = Math.toRadians(p.lat)
        val lon = Math.toRadians(p.lon)
        val sinLat = sin(lat)
        val cosLat = cos(lat)
        val sinLon = sin(lon)
        val cosLon = cos(lon)

        val n = GeoCore.A / sqrt(1 - GeoCore.E2 * sinLat * sinLat)

        val x = (n + p.alt) * cosLat * cosLon
        val y = (n + p.alt) * cosLat * sinLon
        val z = (n * (1 - GeoCore.E2) + p.alt) * sinLat
        return Ecef(x, y, z)
    }

    public fun latLonToWebMercator(p: LatLon): Mercator {
        val R = 6378137.0
        val x = Math.toRadians(p.lon) * R
        val lat = p.lat.coerceIn(-85.05112878, 85.05112878)
        val y = R * ln(tan(Math.PI / 4 + Math.toRadians(lat) / 2))
        return Mercator(x, y)
    }

    fun latLonToUtm(p: LatLon, zoneStr: String = "0", hemisphereNorth: Boolean = true): Utm {
        val crsFactory = CRSFactory()
        val ctFactory = CoordinateTransformFactory()

        val zone = if (zoneStr.toInt() == 0) guessZoneFromLon(p.lon) else zoneStr.toInt()
        if (zone != 0) require(zone in 1..60) { "UTM zone must be 1..60" }
        require(p.lat in -80.0..84.0) { "UTM valid latitude is roughly -80..84" }
        require(p.lon in -180.0..180.0) { "Longitude must be -180..180" }

        val utmParams = buildString {
            append("+proj=utm +zone=$zone ")
            append(if (hemisphereNorth) "" else "+south ")
            append("+datum=WGS84 +units=m +no_defs")
        }

        val src = crsFactory.createFromName("EPSG:4326") // lon/lat
        val dst = crsFactory.createFromParameters("UTM", utmParams)

        val transform = ctFactory.createTransform(src, dst)

        val out = ProjCoordinate()
        // EPSG:4326 expects x=lon, y=lat
        transform.transform(ProjCoordinate(p.lat, p.lon), out)

        val z = if (zone == 0) guessZoneFromLon(p.lon) else zone

        return Utm(
            zone = z,
            hemisphereNorth = hemisphereNorth,
            easting = out.x,
            northing = out.y
        )

    }

    fun latLonToMgrs(p: LatLon, accuracy: Int = 5): String {
        require(accuracy in 0..5) { "MGRS accuracy must be 0..5" }

        // IMPORTANT: from(longitude, latitude)
        val mgrs = MGRS.from(p.lon, p.lat)  // order matters :contentReference[oaicite:1]{index=1}
        return mgrs.coordinate(accuracy)      // not toString(accuracy) :contentReference[oaicite:2]{index=2}
    }

    fun latLonToItm(p: LatLon): Itm {
        val src = ProjCoordinate(p.lon, p.lat) // x=lon, y=lat
        val dst = ProjCoordinate()
        toItmTransform.transform(src, dst)
        return Itm(dst.x, dst.y)
    }

    fun latLonToDms(p: LatLon): Dms {
        fun toDms(value: Double, posHem: Char, negHem: Char): Triple<Int, Int, Pair<Double, Char>> {
            val hem = if (value >= 0) posHem else negHem
            val abs = kotlin.math.abs(value)

            val deg = abs.toInt()
            val minFull = (abs - deg) * 60.0
            val min = minFull.toInt()
            val sec = (minFull - min) * 60.0

            return Triple(deg, min, Pair(sec, hem))
        }

        val (latDeg, latMin, latSecHem) = toDms(p.lat, 'N', 'S')
        val (lonDeg, lonMin, lonSecHem) = toDms(p.lon, 'E', 'W')

        return Dms(
            latDeg = latDeg,
            latMin = latMin,
            latSec = latSecHem.first,
            latHem = latSecHem.second,
            lonDeg = lonDeg,
            lonMin = lonMin,
            lonSec = lonSecHem.first,
            lonHem = lonSecHem.second
        )
    }


    // --------------- HELPER FUNCS ---------------

    fun guessZoneFromLon(lonDeg: Double): Int {
        val z = ((lonDeg + 180.0) / 6.0).toInt() + 1
        return z.coerceIn(1, 60)
    }

}