package com.example.landnv4.domain.geo

import com.example.landnv4.domain.geo.converters.LatLon
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import kotlin.math.*

object DistanceCalc {
    private const val R = 6371008.8 // mean Earth radius meters

    data class Result(
        val distanceMeters: Double,
        val initialBearingDeg: Double,
        val distance3dMeters: Double
    )

    fun haversine(p1: Wgs84, p2: Wgs84): Result {
        val lat1 = Math.toRadians(p1.latDeg)
        val lon1 = Math.toRadians(p1.lonDeg)
        val lat2 = Math.toRadians(p2.latDeg)
        val lon2 = Math.toRadians(p2.lonDeg)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        val d = R * c
        val surface = R * c

        // bearing
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var brng = Math.toDegrees(atan2(y, x))
        brng = (brng + 360.0) % 360.0

        val dz = p2.altMeters - p1.altMeters
        val d3 = sqrt(surface * surface + dz * dz)

        return Result(d, brng, d3)
    }

    /** Straight-line 3D distance using altitude difference + surface distance */
    /*fun distance3D(p1: Wgs84, p2: Wgs84): Double {
        val surface = haversine(p1, p2).distanceMeters
        val dz = p2.altMeters - p1.altMeters
        return sqrt(surface * surface + dz * dz)
    }*/
    fun distance3D(a: Utm, b: Utm, dH: Double): Double {
        val dE = b.easting - a.easting
        val dN = b.northing - a.northing

        return kotlin.math.sqrt(dE*dE + dN*dN + dH*dH)
    }

    fun distanceMeters(a: Utm, b: Utm): Double {

        val de = b.easting - a.easting
        val dn = b.northing - a.northing
        return sqrt(de * de + dn * dn)
    }

    fun metersToFeet(m: Double) = m * 3.280839895

    fun feetToMeters(ft: Double) = ft * 0.3048

}

data class Itm(val easting: Double, val northing: Double)

object ItmConverter {

    private val crsFactory = CRSFactory()
    private val ctFactory = CoordinateTransformFactory()

    private val wgs84 = crsFactory.createFromName("EPSG:4326")

    // ITM / EPSG:2039 (Israel TM Grid)
    private val itm = crsFactory.createFromParameters(
        "ITM",
        "+proj=tmerc +lat_0=31.73439361111111 +lon_0=35.20451694444445 " +
                "+k=1.0000067 +x_0=219529.584 +y_0=626907.39 +ellps=GRS80 " +
                "+towgs84=-48,55,52,0,0,0,0 +units=m +no_defs"
    )

    val wgsToItm = ctFactory.createTransform(wgs84, itm)
    private val itmToWgs = ctFactory.createTransform(itm, wgs84)

    fun wgs84ToItm(latDeg: Double, lonDeg: Double): Itm {
        val src = ProjCoordinate(lonDeg, latDeg) // x=lon, y=lat
        val dst = ProjCoordinate()
        wgsToItm.transform(src, dst)
        return Itm(dst.x, dst.y)
    }

    fun itmToLatLon(easting: Double, northing: Double): LatLon {
        val src = ProjCoordinate(easting, northing)
        val dst = ProjCoordinate()
        itmToWgs.transform(src, dst)
        return LatLon(dst.x, dst.y) // lat, lon
    }
}