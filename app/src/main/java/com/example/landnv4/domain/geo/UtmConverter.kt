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
        val dst = crsFactory.createFromName("EPSG:4326") // WGS84 lat/lon

        val transform = ctFactory.createTransform(src, dst)

        val out = ProjCoordinate()
        transform.transform(
            ProjCoordinate(utm.easting, utm.northing),
            out
        )

        val lon = out.x
        val lat = out.y
        return lat to lon
    }
}
