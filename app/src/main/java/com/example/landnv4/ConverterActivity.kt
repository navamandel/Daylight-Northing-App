package com.example.landnv4

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.landnv4.domain.geo.*
import com.example.landnv4.domain.convert.*

class ConverterActivity : AppCompatActivity() {
/*
    enum class Mode { GEO, ANGLE, DISTANCE }

    enum class GeoInputType { LATLON, DMS, DDM, UTM14, MGRS, ITM, WEB_MERCATOR, ECEF }
    enum class AngleInputType { DEGREES, RADIANS, NATO_MILS, ARTILLERY_MILS, SWEDISH_MILS, GRADIANS, TURNS }
    enum class DistanceInputType { TWO_POINTS, UNIT_VALUE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)

        val spMode = findViewById<Spinner>(R.id.sp_mode)
        val spInputType = findViewById<Spinner>(R.id.sp_input_type)
        val etInput = findViewById<EditText>(R.id.et_input)
        val etZone = findViewById<EditText>(R.id.et_zone)
        val swHemi = findViewById<Switch>(R.id.sw_hemi)
        val btn = findViewById<Button>(R.id.btn_convert)
        val tvOut = findViewById<TextView>(R.id.tv_output)

        val modeItems = listOf("Geo Converter", "Angle Converter", "Distance Converter")
        spMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modeItems)

        fun setGeoTypes() {
            val items = listOf(
                "Lat,Lon,(alt)  e.g. 31.778,35.235,800",
                "DMS  e.g. 31 46 41 N, 35 14 06 E",
                "DDM  e.g. 31 46.683 N, 35 14.100 E",
                "UTM14 (+zone/hemi)  e.g. 06910003748000  (alt optional not supported here)",
                "MGRS string  e.g. 36SYF1234567890",
                "ITM e,n,(alt)  e.g. 219529.584,626907.39,800",
                "Web Mercator (x,y meters)",
                "ECEF (x,y,z meters)"
            )
            spInputType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        }

        fun setAngleTypes() {
            val items = listOf(
                "Degrees",
                "Radians",
                "NATO mils (6400)",
                "Artillery mils (mil-rad)",
                "Swedish mils (6300)",
                "Gradians (gon)",
                "Turns (rev)"
            )
            spInputType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        }

        fun setDistanceTypes() {
            val items = listOf(
                "Two points lat,lon,alt -> lat,lon,alt",
                "Unit value (meters) e.g. 1200"
            )
            spInputType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        }

        fun modeAt(): Mode = when (spMode.selectedItemPosition) {
            0 -> Mode.GEO
            1 -> Mode.ANGLE
            else -> Mode.DISTANCE
        }

        fun geoTypeAt(pos: Int) = when (pos) {
            0 -> GeoInputType.LATLON
            1 -> GeoInputType.DMS
            2 -> GeoInputType.DDM
            3 -> GeoInputType.UTM14
            4 -> GeoInputType.MGRS
            5 -> GeoInputType.ITM
            6 -> GeoInputType.WEB_MERCATOR
            else -> GeoInputType.ECEF
        }

        fun angleTypeAt(pos: Int) = when (pos) {
            0 -> AngleInputType.DEGREES
            1 -> AngleInputType.RADIANS
            2 -> AngleInputType.NATO_MILS
            3 -> AngleInputType.ARTILLERY_MILS
            4 -> AngleInputType.SWEDISH_MILS
            5 -> AngleInputType.GRADIANS
            else -> AngleInputType.TURNS
        }

        fun updateUiForSelection() {
            tvOut.text = ""
            etInput.setText("")

            when (modeAt()) {
                Mode.GEO -> {
                    setGeoTypes()
                    // show zone/hemi only when UTM input is selected
                    etZone.visibility = View.GONE
                    swHemi.visibility = View.GONE
                    swHemi.isChecked = true
                }
                Mode.ANGLE -> {
                    setAngleTypes()
                    etZone.visibility = View.GONE
                    swHemi.visibility = View.GONE
                }
                Mode.DISTANCE -> {
                    setDistanceTypes()
                    etZone.visibility = View.GONE
                    swHemi.visibility = View.GONE
                }
            }
        }

        spMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateUiForSelection()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spInputType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // only show zone/hemi for Geo->UTM14
                val showUtm = (modeAt() == Mode.GEO && position == 1)
                etZone.visibility = if (showUtm) View.VISIBLE else View.GONE
                swHemi.visibility = if (showUtm) View.VISIBLE else View.GONE
                if (showUtm) swHemi.isChecked = true
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // initial
        updateUiForSelection()

        btn.setOnClickListener {
            try {
                val input = etInput.text.toString().trim()
                if (input.isBlank()) throw IllegalArgumentException("Input is empty")



                val output = if (modeAt() == Mode.GEO) {
                    val type = geoTypeAt(spInputType.selectedItemPosition)

                    // UTM params only needed for UTM input
                    if (type == GeoInputType.UTM14) {
                        etZone.visibility = View.VISIBLE
                        swHemi.visibility = View.VISIBLE
                        swHemi.isChecked = true
                    } else {
                        etZone.visibility = View.GONE
                        swHemi.visibility = View.GONE
                    }

                    val zone = etZone.text.toString().trim().toIntOrNull() ?: 36
                    val hemiNorth = swHemi.isChecked

                    val (lat, lon) = parseGeoInputToLatLon(type, input, zone, hemiNorth)
                    formatAllGeoOutputs(lat, lon, zone, hemiNorth)
                } else {
                    val t = angleTypeAt(spInputType.selectedItemPosition)
                    val rad = parseAngleToRadians(t, input.toDouble())
                    formatAllAngleOutputs(rad)
                }

                tvOut.text = output
            } catch (e: Exception) {
                tvOut.text = "Error: ${e.message}"
            }
        }
    }

    private fun parseGeoInputToLatLon(
        type: GeoInputType,
        input: String,
        zone: Int,
        hemiNorth: Boolean
    ): Pair<Double, Double> {
        return when (type) {
            GeoInputType.LATLON -> {
                // "lat,lon"
                val parts = input.split(",", " ", "\t").filter { it.isNotBlank() }
                if (parts.size < 2) throw IllegalArgumentException("Lat,Lon format: 31.778,35.235")
                val lat = parts[0].toDouble()
                val lon = parts[1].toDouble()
                lat to lon
            }

            GeoInputType.DMS -> {
                // "31 46 41 N, 35 14 06 E"
                val parts = input.replace(",", " ").split(" ").filter { it.isNotBlank() }
                if (parts.size < 8) throw IllegalArgumentException("DMS format: 31 46 41 N, 35 14 06 E")
                val lat = GeoFormats.decimalToDms(0.0, true) // dummy for access? no
                val latDeg = GeoFormatsRun.dmsToDecimal(parts[0], parts[1], parts[2], parts[3][0])
                val lonDeg = GeoFormatsRun.dmsToDecimal(parts[4], parts[5], parts[6], parts[7][0])
                latDeg to lonDeg
            }

            GeoInputType.DDM -> {
                // "31 46.683 N, 35 14.100 E"
                val p = input.replace(",", " ").split(" ").filter { it.isNotBlank() }
                if (p.size < 6) throw IllegalArgumentException("DDM format: 31 46.683 N, 35 14.100 E")
                val lat = ddmToDecimal(p[0].toInt(), p[1].toDouble(), p[2][0])
                val lon = ddmToDecimal(p[3].toInt(), p[4].toDouble(), p[5][0])
                lat to lon
            }

            GeoInputType.UTM14 -> {
                val utm = UtmParser.parse14Digits(input, zone, hemiNorth)
                UtmConverter.toLatLonWgs84(utm)
            }

            GeoInputType.MGRS -> {
                MgrsConverter.mgrsToLatLon(input)
            }

            GeoInputType.ITM -> {
                // "easting,northing"
                val parts = input.split(",", " ", "\t").filter { it.isNotBlank() }
                if (parts.size < 2) throw IllegalArgumentException("ITM format: easting,northing")
                val e = parts[0].toDouble()
                val n = parts[1].toDouble()
                ItmConverter.itmToWgs84(e, n)
            }

            GeoInputType.WEB_MERCATOR -> {
                val parts = input.split(",", " ", "\t").filter { it.isNotBlank() }
                if (parts.size < 2) throw IllegalArgumentException("Web Mercator format: x,y")
                val x = parts[0].toDouble()
                val y = parts[1].toDouble()
                webMercatorToLatLon(x, y)
            }

            GeoInputType.ECEF -> {
                val parts = input.split(",", " ", "\t").filter { it.isNotBlank() }
                if (parts.size < 3) throw IllegalArgumentException("ECEF format: x,y,z")
                val ecef = GeoFormats.Ecef(parts[0].toDouble(), parts[1].toDouble(), parts[2].toDouble())
                ecefToLatLon(ecef)
            }
        } as Pair<Double, Double>
    }

    private fun formatAllGeoOutputs(lat: Double, lon: Double, zone: Int, hemiNorth: Boolean): String {
        val sb = StringBuilder()

        sb.appendLine("WGS84 Decimal:")
        sb.appendLine("lat=%.6f, lon=%.6f".format(lat, lon))
        sb.appendLine()

        sb.appendLine("DMS:")
        val latDms = GeoFormats.decimalToDms(lat, true)
        val lonDms = GeoFormats.decimalToDms(lon, false)
        sb.appendLine("${GeoFormats.formatDms(latDms)}, ${GeoFormats.formatDms(lonDms)}")
        sb.appendLine()

        sb.appendLine("DDM:")
        sb.appendLine("${GeoFormats.formatDdm(lat, true)}, ${GeoFormats.formatDdm(lon, false)}")
        sb.appendLine()

        sb.appendLine("UTM:")
        val utm = UtmConverter.fromLatLonWgs84(lat, lon, zone, hemiNorth) // you may need this method
        sb.appendLine("zone=${utm.zone}${if (utm.hemisphereNorth) "N" else "S"} e=${utm.easting} n=${utm.northing}")
        sb.appendLine()

        sb.appendLine("MGRS:")
        sb.appendLine(MgrsConverter.latLonToMgrs(lat, lon))
        sb.appendLine()

        sb.appendLine("ITM (EPSG:2039):")
        val itm = ItmConverter.wgs84ToItm(lat, lon)
        sb.appendLine("e=%.3f, n=%.3f".format(itm.easting, itm.northing))
        sb.appendLine()

        sb.appendLine("Web Mercator (EPSG:3857):")
        val merc = GeoFormats.latLonToWebMercator(lat, lon)
        sb.appendLine("x=%.3f, y=%.3f".format(merc.x, merc.y))
        sb.appendLine()

        sb.appendLine("ECEF (WGS84, alt=0):")
        val ecef = GeoFormats.latLonToEcef(lat, lon, 0.0)
        sb.appendLine("x=%.3f, y=%.3f, z=%.3f".format(ecef.x, ecef.y, ecef.z))

        return sb.toString()
    }

    private fun parseAngleToRadians(type: AngleInputType, value: Double): Double {
        return when (type) {
            AngleInputType.DEGREES -> AngleConverter.degreesToRadians(value)
            AngleInputType.RADIANS -> value
            AngleInputType.NATO_MILS -> AngleConverter.milsNatoToRadians(value)
            AngleInputType.ARTILLERY_MILS -> AngleConverter.milsArtilleryToRadians(value)
            AngleInputType.SWEDISH_MILS -> AngleConverter.milsSwedishToRadians(value)
            AngleInputType.GRADIANS -> AngleConverter.gradiansToRadians(value)
            AngleInputType.TURNS -> AngleConverter.turnsToRadians(value)
        }
    }

    private fun formatAllAngleOutputs(rad: Double): String {
        val deg = AngleConverter.radiansToDegrees(rad)
        return buildString {
            appendLine("Radians: %.8f".format(rad))
            appendLine("Degrees: %.6f".format(deg))
            appendLine("Gradians (gon): %.6f".format(AngleConverter.radiansToGradians(rad)))
            appendLine("Turns (rev): %.8f".format(AngleConverter.radiansToTurns(rad)))
            appendLine("NATO mils (6400): %.3f".format(AngleConverter.radiansToMilsNato(rad)))
            appendLine("Artillery mils (mil-rad): %.3f".format(AngleConverter.radiansToMilsArtillery(rad)))
            appendLine("Swedish mils (6300): %.3f".format(AngleConverter.radiansToMilsSwedish(rad)))
        }
    }

    // Helpers for DMS/DDM parsing + WebMercator inverse + ECEF inverse

    private fun ddmToDecimal(deg: Int, minutes: Double, hemi: Char): Double {
        val sign = if (hemi.uppercaseChar() == 'S' || hemi.uppercaseChar() == 'W') -1 else 1
        return sign * (kotlin.math.abs(deg).toDouble() + minutes / 60.0)
    }

    private object GeoFormatsRun {
        fun dmsToDecimal(d: String, m: String, s: String, hemi: Char): Double {
            val dd = d.toInt()
            val mm = m.toInt()
            val ss = s.toDouble()
            val sign = if (hemi.uppercaseChar() == 'S' || hemi.uppercaseChar() == 'W') -1 else 1
            return sign * (kotlin.math.abs(dd).toDouble() + mm / 60.0 + ss / 3600.0)
        }
    }

    private fun webMercatorToLatLon(x: Double, y: Double): Pair<Double, Double> {
        val R = 6378137.0
        val lon = Math.toDegrees(x / R)
        val lat = Math.toDegrees(2 * Math.atan(Math.exp(y / R)) - Math.PI / 2)
        return lat to lon
    }

    private fun ecefToLatLon(ecef: GeoFormats.Ecef): Pair<Double, Double> {
        // reuse GeoFormats latLonToEcef’s constants in reverse (approx)
        val a = 6378137.0
        val f = 1.0 / 298.257223563
        val e2 = f * (2 - f)

        val x = ecef.x
        val y = ecef.y
        val z = ecef.z

        val lon = Math.atan2(y, x)
        val p = kotlin.math.sqrt(x * x + y * y)

        var lat = Math.atan2(z, p * (1 - e2))
        repeat(5) {
            val sinLat = kotlin.math.sin(lat)
            val N = a / kotlin.math.sqrt(1 - e2 * sinLat * sinLat)
            lat = Math.atan2(z + e2 * N * sinLat, p)
        }

        return Math.toDegrees(lat) to Math.toDegrees(lon)
    }*/
}
