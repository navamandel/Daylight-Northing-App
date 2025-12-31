package com.example.landnv4.domain.geo

import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

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

    private val wgsToItm = ctFactory.createTransform(wgs84, itm)
    private val itmToWgs = ctFactory.createTransform(itm, wgs84)

    fun wgs84ToItm(latDeg: Double, lonDeg: Double): Itm {
        val src = ProjCoordinate(lonDeg, latDeg) // x=lon, y=lat
        val dst = ProjCoordinate()
        wgsToItm.transform(src, dst)
        return Itm(dst.x, dst.y)
    }

    fun itmToWgs84(easting: Double, northing: Double): Pair<Double, Double> {
        val src = ProjCoordinate(easting, northing)
        val dst = ProjCoordinate()
        itmToWgs.transform(src, dst)
        return dst.y to dst.x // lat, lon
    }
}
