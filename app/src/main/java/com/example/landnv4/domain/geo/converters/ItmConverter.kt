package com.example.landnv4.domain.geo.converters

import android.util.Log
import com.example.landnv4.domain.geo.InputParsing.isFiniteReal
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

data class Itm(
    val easting: Double,   // meters
    val northing: Double   // meters
)

object ItmConverter {

    // EPSG:2039 (Israel 1993 / Israeli TM Grid) as explicit PROJ string.
    // Source: epsg.io "proj4js" definition for EPSG:2039. :contentReference[oaicite:1]{index=1}
    private const val ITM_2039_PROJ4 =
        "+proj=tmerc +lat_0=31.73439361111111 +lon_0=35.20451694444444 " +
                "+k=1.0000067 +x_0=219529.584 +y_0=626907.39 " +
                "+ellps=GRS80 +units=m +no_defs"
        /*
        "+proj=tmerc +lat_0=31.7343936111111 +lon_0=35.2045169444444 " +
                "+k=1.0000067 +x_0=219529.584 +y_0=626907.39 +ellps=GRS80 " +
                "+towgs84=23.772,17.49,17.859,-0.3132,-1.85274,1.67299,-5.4262 " +
                "+units=m +no_defs +type=crs"

         */

    private val crsFactory by lazy { CRSFactory() }
    private val ctFactory by lazy { CoordinateTransformFactory() }

    private val itmCrs: CoordinateReferenceSystem by lazy {
        crsFactory.createFromParameters("EPSG:2039", ITM_2039_PROJ4)
    }

    private val wgs84Crs: CoordinateReferenceSystem by lazy {
        // WGS84 geographic (lon/lat in degrees)
        crsFactory.createFromParameters("WGS84", "+proj=longlat +datum=WGS84 +no_defs")
    }

    fun validate(p: Itm) {
        if (!p.easting.isFiniteReal()) throw IllegalArgumentException("ITM easting must be a real number")
        if (!p.northing.isFiniteReal()) throw IllegalArgumentException("ITM northing must be a real number")
    }

    fun parse(inputE: String, inputN: String): Itm {
        /*val ee = e.trim().toDoubleOrNull() ?: error("Invalid ITM easting")
        val nn = n.trim().toDoubleOrNull() ?: error("Invalid ITM northing")
        val p = Itm(ee, nn)
        val errs = validate(p)
        require(errs.isEmpty()) { errs.joinToString("; ") }
        return p*/

        val e = inputE.toDouble()
        val n = inputN.toDouble()
        val p = Itm(e, n)

        try {
            validate(p)
            return p
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun format(p: Itm): String = "E=%.3f N=%.3f".format(p.easting, p.northing)


    /**
     * Convert ITM (EPSG:2039) -> WGS84 latitude/longitude.
     *
     * @param itm easting/northing in meters
     * @return LatLon in degrees (lat, lon)
     */
    fun itmToLatLon(itm: Itm): LatLon {
        val transform = ctFactory.createTransform(itmCrs, wgs84Crs)

        val src = ProjCoordinate(itm.easting, itm.northing)
        val dst = ProjCoordinate()

        transform.transform(src, dst)

        // Proj4J returns (x=lon, y=lat) for geographic CRS
        return LatLon(
            lat = dst.y,
            lon = dst.x
        )
    }
}
