package com.example.landnv4.domain.geo

import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

object UtmConverter {

    /**
     * Converts UTM -> (lat, lon) in WGS84.
     * Returns Pair(latDeg, lonDeg)
     */
    fun toLatLonWgs84(utm: Utm): Pair<Double, Double> {
        val crsFactory = CRSFactory()
        val ctFactory = CoordinateTransformFactory()

        val zone = utm.zone
        require(zone in 1..60) { "UTM zone must be 1..60" }

        val utmParams = buildString {
            append("+proj=utm +zone=$zone ")
            append(if (utm.hemisphereNorth) "" else "+south ")
            append("+datum=WGS84 +units=m +no_defs")
        }

        val src = crsFactory.createFromParameters("UTM", utmParams)
        val dst = crsFactory.createFromName("EPSG:4326") // lon/lat

        val transform = ctFactory.createTransform(src, dst)

        val out = ProjCoordinate()
        transform.transform(ProjCoordinate(utm.easting, utm.northing), out)

        // EPSG:4326: x=lon, y=lat
        return out.y to out.x
    }

    /**
     * Converts (lat, lon) WGS84 -> UTM.
     * You must supply zone + hemisphere (because requirement wants explicit inputs).
     */
    fun fromLatLonWgs84(latDeg: Double, lonDeg: Double, zone: Int, hemisphereNorth: Boolean): Utm {
        val crsFactory = CRSFactory()
        val ctFactory = CoordinateTransformFactory()

        require(zone in 1..60) { "UTM zone must be 1..60" }
        require(latDeg in -80.0..84.0) { "UTM valid latitude is roughly -80..84" }
        require(lonDeg in -180.0..180.0) { "Longitude must be -180..180" }

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
        transform.transform(ProjCoordinate(lonDeg, latDeg), out)

        return Utm(
            zone = zone,
            hemisphereNorth = hemisphereNorth,
            easting = out.x,
            northing = out.y
        )
    }

    /**
     * Optional helper: recommended zone computed from longitude (if you want auto-zone).
     */
    fun guessZoneFromLon(lonDeg: Double): Int {
        val z = ((lonDeg + 180.0) / 6.0).toInt() + 1
        return z.coerceIn(1, 60)
    }
}
