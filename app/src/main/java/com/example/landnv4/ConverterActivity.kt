package com.example.landnv4

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landnv4.databinding.ActivityConverterBinding
import com.example.landnv4.databinding.ActivityHomeBinding
import com.example.landnv4.databinding.ActivityPointsListBinding
import com.example.landnv4.databinding.IncludeFormBinding
import com.example.landnv4.databinding.IncludeResultsBinding
import com.example.landnv4.domain.geo.*
import com.example.landnv4.domain.convert.*
import com.example.landnv4.domain.geo.GeoCore
import com.example.landnv4.domain.geo.UtmParser.parseUtm
import com.example.landnv4.domain.geo.converters.Ecef
import com.example.landnv4.domain.geo.converters.LatLon
import com.example.landnv4.domain.geo.converters.Mercator
import com.example.landnv4.domain.geo.converters.Itm
import com.example.landnv4.domain.geo.converters.Dms

import com.example.landnv4.domain.geo.converters.LatLonConverter
import com.example.landnv4.domain.geo.converters.MgrsConverter
import com.example.landnv4.domain.geo.converters.UtmConverter
import com.example.landnv4.domain.geo.converters.MercatorConverter
import com.example.landnv4.domain.geo.converters.EcefConverter
import com.example.landnv4.domain.geo.converters.ItmConverter
import com.example.landnv4.domain.geo.converters.DmsConverter
import com.example.landnv4.ui.ExpandableSection
import com.example.landnv4.ui.ResultItem
import com.example.landnv4.ui.ResultsAdapter
import com.example.landnv4.ui.form.FormAdapter
import com.example.landnv4.ui.form.FormItem
import com.example.landnv4.ui.form.FormState
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView


class ConverterActivity : BaseActivity() {

    enum class Mode { GEO, ANGLE, DISTANCE }

    enum class GeoInputType { LATLON, DMS, UTM, MGRS, ITM, WEB_MERCATOR, ECEF }
    enum class AngleInputType { DEGREES, RADIANS, NATO_MILS, ARTILLERY_MILS, SWEDISH_MILS, GRADIANS, TURNS }
    enum class DistanceInputType { METERS, KILOMETERS, FEET, MILES, NAUTICAL_MILES }
    private lateinit var binding: ActivityConverterBinding
    private lateinit var resultsBinding: IncludeResultsBinding
    private lateinit var secR: ExpandableSection
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var formAdapter: FormAdapter
    private val KEY_CATEGORY = "category"
    private val KEY_UNIT = "unit"
    private val KEY_VALUE = "value"
    override fun getLayoutResId() = R.layout.activity_converter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = contentContainer.getChildAt(0)
        binding = ActivityConverterBinding.bind(root)
        setupToolbar("Converter")

        val themedResInflater = LayoutInflater.from(
            ContextThemeWrapper(this, R.style.ThemeOverlay_LandN_Results_A)
        )
        resultsBinding = IncludeResultsBinding.inflate(
            themedResInflater,
            binding.sectionResults.findViewById<FrameLayout>(R.id.content),
            true
        )

        secR = ExpandableSection(
            root = binding.sectionResults.findViewById<MaterialCardView>(R.id.card),
            header = binding.sectionResults.findViewById<LinearLayout>(R.id.header),
            content = binding.sectionResults.findViewById<FrameLayout>(R.id.content),
            btnToggle = binding.sectionResults.findViewById(R.id.btnToggle),
            btnClear = binding.sectionResults.findViewById(R.id.btnClear)
        ).apply {
            setTitle("Results")
            setClearVisible(true)
            wireClicks()
        }

        val modeItems = listOf("Geo Converter", "Angle Converter", "Distance Converter")

        val hints = mutableMapOf(
            "hint_lat" to "Latitude",
            "hint_easting" to "Easting",
            "hint_x_coord" to "X-Coordinate",

            "hint_lon" to "Longitude",
            "hint_northing" to "Northing",
            "hint_y_coord" to "Y-Coordinate"
        )


        val items = listOf(
            FormItem.Spinner(
                key = KEY_CATEGORY,
                label = "Converter Type",
                staticOptions = listOf(
                    Mode.GEO.name to modeItems[0],
                    Mode.ANGLE.name to modeItems[1],
                    Mode.DISTANCE.name to modeItems[2]
                )
            ),
            FormItem.Spinner(
                key = KEY_UNIT,
                label = "Units",
                dependsOnKey = KEY_CATEGORY,
                optionsProvider = { parent -> when(parent) {
                    Mode.GEO.name -> listOf(
                        GeoInputType.LATLON.name to setGeoTypes(GeoInputType.LATLON),
                        GeoInputType.DMS.name to setGeoTypes(GeoInputType.DMS),
                        GeoInputType.UTM.name to setGeoTypes(GeoInputType.UTM),
                        GeoInputType.MGRS.name to setGeoTypes(GeoInputType.MGRS),
                        GeoInputType.ITM.name to setGeoTypes(GeoInputType.ITM),
                        GeoInputType.WEB_MERCATOR.name to setGeoTypes(GeoInputType.WEB_MERCATOR),
                        GeoInputType.ECEF.name to setGeoTypes(GeoInputType.ECEF)
                    )
                    Mode.ANGLE.name -> listOf(
                        AngleInputType.DEGREES.name to setAngleTypes(AngleInputType.DEGREES),
                        AngleInputType.RADIANS.name to setAngleTypes(AngleInputType.RADIANS),
                        AngleInputType.NATO_MILS.name to setAngleTypes(AngleInputType.NATO_MILS),
                        AngleInputType.ARTILLERY_MILS.name to setAngleTypes(AngleInputType.ARTILLERY_MILS),
                        AngleInputType.SWEDISH_MILS.name to setAngleTypes(AngleInputType.SWEDISH_MILS),
                        AngleInputType.GRADIANS.name to setAngleTypes(AngleInputType.GRADIANS),
                        AngleInputType.TURNS.name to setAngleTypes(AngleInputType.TURNS)
                        )
                    Mode.DISTANCE.name -> listOf(
                        DistanceInputType.METERS.name to setDistanceTypes(DistanceInputType.METERS),
                        DistanceInputType.KILOMETERS.name to setDistanceTypes(DistanceInputType.KILOMETERS),
                        DistanceInputType.FEET.name to setDistanceTypes(DistanceInputType.FEET),
                        DistanceInputType.MILES.name to setDistanceTypes(DistanceInputType.MILES),
                        DistanceInputType.NAUTICAL_MILES.name to setDistanceTypes(DistanceInputType.NAUTICAL_MILES)
                    )
                    else -> emptyList()
                } }
            ),
            FormItem.Coords(
                KEY_VALUE,
                "Enter Value(s)",
                true,
                hintsLabels = hints
            )
        )

        formAdapter = FormAdapter(items)
        binding.includeForm.formTitle.visibility = View.GONE
        //binding.includeForm.formSubtitle.visibility = View.GONE
        binding.includeForm.rvInput.layoutManager = LinearLayoutManager(this)
        binding.includeForm.rvInput.adapter = formAdapter

        resultsAdapter = ResultsAdapter()

        resultsBinding.rvResults.adapter = resultsAdapter
        resultsBinding.rvResults.layoutManager = LinearLayoutManager(this)
        resultsBinding.rvResults.isNestedScrollingEnabled = false
        resultsBinding.tvResultsTitle.visibility = View.GONE

        /*binding.btnConvert.setOnClickListener {
            resultsAdapter.submitList(results)
        }*/

        formAdapter.state.addListener { key ->
            if (key == KEY_CATEGORY || key == KEY_UNIT) {
                secR.setExpanded(false)
            }
        }



        /*fun modeAt(): Mode = when (spMode.selectedItemPosition) {
            0 -> Mode.GEO
            1 -> Mode.ANGLE
            else -> Mode.DISTANCE
        }*/

        fun geoTypeAt(pos: Int) = when (pos) {
            0 -> GeoInputType.LATLON
            1 -> GeoInputType.DMS
            2 -> GeoInputType.UTM
            3 -> GeoInputType.MGRS
            4 -> GeoInputType.ITM
            5 -> GeoInputType.WEB_MERCATOR
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

        fun distanceTypeAt(pos: Int) = when (pos) {
            0 -> DistanceInputType.METERS
            1 -> DistanceInputType.KILOMETERS
            2 -> DistanceInputType.FEET
            3 -> DistanceInputType.MILES
            else -> DistanceInputType.NAUTICAL_MILES
        }

        /*fun updateUiForSelection() {
            resultsAdapter.submitList(emptyList())
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

        // initial
        updateUiForSelection()

        spInputType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val showUtm = (modeAt() == Mode.GEO && geoTypeAt(position) == GeoInputType.UTM13)
                etZone.visibility = if (showUtm) View.VISIBLE else View.GONE
                swHemi.visibility = if (showUtm) View.VISIBLE else View.GONE
                if (!showUtm) etZone.setText("")  // optional: clear to avoid confusion
                if (showUtm) swHemi.isChecked = true
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }*/

        binding.btnConvert.setOnClickListener {
            // Optional: prevent double taps during computation
            binding.btnConvert.isEnabled = false

            try {
                formAdapter.clearAllErrors() // if you have this; otherwise ignore

                val category = formAdapter.state.getString(KEY_CATEGORY)
                if (category.isNullOrBlank()) {
                    //formAdapter.setError(KEY_CATEGORY, "Choose a converter type")
                    return@setOnClickListener
                }

                val unit = formAdapter.state.getString(KEY_UNIT)
                if (unit.isNullOrBlank()) {
                    //formAdapter.setError(KEY_UNIT, "Choose units/type")
                    return@setOnClickListener
                }

                val results = when (Mode.valueOf(category)) {
                    Mode.GEO -> {
                        val type = GeoInputType.valueOf(unit)

                        // Fetch only what the selected type needs:
                        val ex = formAdapter.state.getString("ex_coord").orEmpty()
                        val ny = formAdapter.state.getString("ny_coord").orEmpty()
                        val z  = formAdapter.state.getString("z_coord").orEmpty()
                        val zone = formAdapter.state.getString("utm_zone") ?: "0"
                        val hemi = formAdapter.state.getBoolean("utm_hemisphere") ?: true

                        val latlon = parseGeoInputToLatLon(
                            type = type,
                            inputEX = ex,
                            inputNY = ny,
                            inputZ = z,
                            zone = zone,
                            hemiNorth = hemi
                        )

                        formatAllGeoOutputs(latlon, zone, hemi)
                    }

                    Mode.ANGLE -> {
                        val v = formAdapter.state.getString("ex_coord")?.toDoubleOrNull()
                        if (v == null) {
                            binding.includeForm.tvError.text = "Enter a number"
                            return@setOnClickListener
                        }
                        val ang = parseAngleToRadians(AngleInputType.valueOf(unit), v)
                        formatAllAngleOutputs(ang)
                    }

                    Mode.DISTANCE -> {
                        val v = formAdapter.state.getString("ex_coord")
                        if (v.isNullOrBlank()) {
                            binding.includeForm.tvError.text = "Enter a number"
                            return@setOnClickListener
                        }
                        parseDistance(DistanceInputType.valueOf(unit), v)
                    }
                }

                binding.includeForm.tvError.visibility = View.GONE
                resultsAdapter.submitList(results)
                binding.sectionResults.visibility = View.VISIBLE
                secR.setExpanded(true)

                // IMPORTANT: don't clear everything.
                // Either do nothing, or clear only specific text inputs:
                //formAdapter.state.set("ex_coord", "")
                //formAdapter.state.set("ny_coord", "")
                //formAdapter.state.set("z_coord", "")
                //formAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("Converter", "Convert failed", e)
                binding.includeForm.tvError.visibility = View.VISIBLE
                binding.includeForm.tvError.text = e.message ?: "Conversion failed"
            } finally {
                binding.btnConvert.isEnabled = true
            }
        }


        /*binding.btnConvert.setOnClickListener {
            try {
                val category = formAdapter.state.getString(KEY_CATEGORY) ?: return@setOnClickListener
                val unit = formAdapter.state.getString(KEY_UNIT) ?: return@setOnClickListener

                val zone = formAdapter.state.getString("utm_zone")?.toInt() ?: 0
                val hemi = formAdapter.state.getBoolean("utm_hemisphere") ?: true

                val results = when(Mode.valueOf(category)) {
                    Mode.GEO -> {
                        val latlon = parseGeoInputToLatLon(
                            type = GeoInputType.valueOf(unit),
                            inputEX = formAdapter.state.getString("ex_coord") ?: "error",
                            inputNY = formAdapter.state.getString("ny_coord")?: "error",
                            inputZ = formAdapter.state.getString("z_coord")?: "error",
                            zone = zone,
                            hemiNorth = hemi
                        )
                        Log.d("LatLon:", "lat= ${latlon.lat}, lon=${latlon.lon}")

                        formatAllGeoOutputs(latlon, zone, hemi)
                    }

                    Mode.ANGLE -> {
                        val ang = parseAngleToRadians(
                            AngleInputType.valueOf(unit),
                            formAdapter.state.getString("ex_coord")!!.toDouble()
                        )
                        formatAllAngleOutputs(ang)
                    }

                    Mode.DISTANCE -> parseDistance(
                        DistanceInputType.valueOf(unit),
                        formAdapter.state.getString("ex_coord")!!
                    )
                }

                resultsAdapter.submitList(results)
                //formAdapter.state.clearAllValues()

            } catch (e: Exception) {
                formAdapter.setError(KEY_VALUE, e.message)
            }

        }

        btn.setOnClickListener {
            try {
                if (resultsAdapter.itemCount > 0) updateUiForSelection()
                val input = etInput.text.toString().trim()
                if (input.isBlank()) throw IllegalArgumentException("Input is empty")

                val type = spInputType.selectedItemPosition

                val results = when (modeAt()) {
                    Mode.GEO -> {
                        val geoType = geoTypeAt(type)

                        val showUtm = (geoType == GeoInputType.UTM13)
                        val zone = if (showUtm) {
                            etZone.text.toString().trim().toIntOrNull()
                                ?: 36
                        } else 0

                        val latlon = parseGeoInputToLatLon(
                            geoType,
                            input,
                            zone,
                            swHemi.isChecked
                        )

                        formatAllGeoOutputs(latlon, zone, swHemi.isChecked)
                    }

                    Mode.ANGLE -> {
                        val ang = parseAngleToRadians(angleTypeAt(type), input.toDouble())
                        formatAllAngleOutputs(ang)
                    }
                    Mode.DISTANCE -> parseDistance(distanceTypeAt(type),input, type)
                }

                // tvOut.text = out
                resultsAdapter.submitList(results)
            } catch (e: Exception) {
                // tvOut.text = "Error: ${e.message}"
                resultsAdapter.submitList(listOf(ResultItem("Error", e.message ?: "Unknown error")))
            }
        }*/
    }

    override fun onStop() {
        super.onStop()
        formAdapter.state.clearAllValues()
    }


    fun setGeoTypes(geoType: GeoInputType): String {
        return when (geoType) {
            GeoInputType.LATLON -> "Lat,Lon  e.g. 31.778, 35.235"
            GeoInputType.DMS -> "DMS  e.g. 31 46 41 N, 35 14 06 E"
            GeoInputType.UTM -> "UTM (+zone/hemi)  e.g. 069100, 0374800 "
            GeoInputType.MGRS -> "MGRS string  e.g. 36SYF1234567890"
            GeoInputType.ITM -> "ITM e,n  e.g. 219529.584, 626907.39"
            GeoInputType.WEB_MERCATOR -> "Web Mercator (x,y meters)"
            GeoInputType.ECEF -> "ECEF (x,y,z meters)"
        }
        //spInputType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

    }

    fun setAngleTypes(angleType: AngleInputType): String {
        return when(angleType) {
            AngleInputType.DEGREES -> "Degrees"
            AngleInputType.RADIANS -> "Radians"
            AngleInputType.NATO_MILS -> "NATO mils (6400)"
            AngleInputType.ARTILLERY_MILS -> "Artillery mils (mil-rad)"
            AngleInputType.SWEDISH_MILS -> "Swedish mils (6300)"
            AngleInputType.GRADIANS -> "Gradians (gon)"
            AngleInputType.TURNS -> "Turns (rev)"
        }
        //spInputType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

    }

    fun setDistanceTypes(distType: DistanceInputType): String {
        return when(distType) {
            DistanceInputType.METERS -> "Meters"
            DistanceInputType.KILOMETERS -> "Kilometers"
            DistanceInputType.FEET -> "Feet"
            DistanceInputType.MILES -> "Miles"
            DistanceInputType.NAUTICAL_MILES -> "Nautical Miles"
        }
        //spInputType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

    }

    private fun parseGeoInputToLatLon(
        type: GeoInputType,
        inputEX: String,
        inputNY: String = "",
        inputZ: String = "",
        zone: String = "",
        hemiNorth: Boolean

    ): LatLon {
        try {
            binding.includeForm.tvError.visibility = View.GONE
            return when (type) {
                // "lat,lon"
                GeoInputType.LATLON -> {
                    LatLonConverter.parse(inputEX, inputNY)
                }

                // "31 46 41 N, 35 14 06 E"
                GeoInputType.DMS -> {
                    val dms = DmsConverter.parse(inputEX)
                    DmsConverter.dmsToLatLon(dms)
                }

                GeoInputType.UTM -> {
                    val utm = parseUtm(inputEX, inputNY, zone, hemiNorth)
                    UtmConverter.utmToLatLon(utm)
                }

                GeoInputType.MGRS -> {
                    MgrsConverter.mgrsToLatLon(inputEX)
                }

                GeoInputType.ITM -> {
                    // "easting,northing"
                    val itm = ItmConverter.parse(inputEX, inputNY)
                    ItmConverter.itmToLatLon(itm)
                }

                GeoInputType.WEB_MERCATOR -> {
                    val wm = MercatorConverter.parse(inputEX, inputNY)
                    MercatorConverter.webMercatorToLatLon(wm)
                }

                GeoInputType.ECEF -> {
                    val ecef = EcefConverter.parse(inputEX, inputNY, inputZ)
                    EcefConverter.ecefToLatLon(ecef)
                }

            }
        } catch (e: Exception) {
            binding.includeForm.tvError.visibility = View.VISIBLE
            binding.includeForm.tvError.text = e.message ?: "Conversion error"
            throw e
        }
        //return null
    }


    /*sb.appendLine("WGS84 Decimal:")
    sb.appendLine("lat=%.6f, lon=%.6f".format(latlon.lat, latlon.lon))
    sb.appendLine()

    sb.appendLine("DMS:")
    val latDms = GeoFormats.decimalToDms(latlon.lat, true)
    val lonDms = GeoFormats.decimalToDms(latlon.lon, false)
    sb.appendLine("${GeoFormats.formatDms(latDms)}, ${GeoFormats.formatDms(lonDms)}")
    sb.appendLine()

    sb.appendLine("UTM:")
    val z = if (zone in 1..60) zone else autoUtmZone(latlon.lon)
    val utm = UtmConverter.fromLatLonWgs84(latlon.lat, latlon.lon, z, hemiNorth)
    sb.appendLine("zone=${utm.zone}${if (utm.hemisphereNorth) "N" else "S"} e=${utm.easting} n=${utm.northing}")
    sb.appendLine()


    sb.appendLine("MGRS:")
    sb.appendLine(MgrsConverter.latLonToMgrs(latlon.lat, latlon.lon))
    sb.appendLine()

    sb.appendLine("ITM (EPSG:2039):")
    val itm = LatLonConverter.latLonToItm(latlon.lat, latlon.lon)
    sb.appendLine("e=%.3f, n=%.3f".format(itm.easting, itm.northing))
    sb.appendLine()

    sb.appendLine("Web Mercator (EPSG:3857):")
    val merc = GeoFormats.latLonToWebMercator(latlon.lat, latlon.lon)
    sb.appendLine("x=%.3f, y=%.3f".format(merc.x, merc.y))
    sb.appendLine()

    sb.appendLine("ECEF (WGS84, alt=0):")
    val ecef = GeoFormats.latLonToEcef(latlon.lat, latlon.lon, 0.0)
    sb.appendLine("x=%.3f, y=%.3f, z=%.3f".format(ecef.x, ecef.y, ecef.z))

    val sb = StringBuilder()

        sb.appendLine(LatLonConverter.format(latlon))
        sb.appendLine()

        sb.appendLine(DmsConverter.format(LatLonConverter.latLonToDms(latlon)))
        sb.appendLine()

        sb.appendLine(UtmConverter.format(LatLonConverter.latLonToUtm(latlon, zone, hemiNorth)))
        sb.appendLine()

        sb.appendLine(MgrsConverter.format(LatLonConverter.latLonToMgrs(latlon)))
        sb.appendLine()

        sb.appendLine(ItmConverter.format(LatLonConverter.latLonToItm(latlon)))
        sb.appendLine()

        sb.appendLine(MercatorConverter.format(LatLonConverter.latLonToWebMercator(latlon)))
        sb.appendLine()

        sb.appendLine(EcefConverter.format(LatLonConverter.latLonToEcef(latlon)))

        return sb.toString()*/
    private fun formatAllGeoOutputs(latlon: LatLon, zone: String = "0", hemiNorth: Boolean = true): List<ResultItem> {
        val utmValue = if (latlon.lat !in -80.0..84.0) "Utm not defined"
                       else UtmConverter.format(LatLonConverter.latLonToUtm(latlon, zone, hemiNorth))

        val wbIsClamped = if (latlon.lat !in -85.0511..85.0511) " (Clamped)" else ""

        return listOf(
            ResultItem("WGS84 Decimal", LatLonConverter.format(latlon)),
            ResultItem("DMS", DmsConverter.format(LatLonConverter.latLonToDms(latlon))),
            ResultItem("UTM", utmValue),
            ResultItem("MGRS", MgrsConverter.format(LatLonConverter.latLonToMgrs(latlon))),
            ResultItem("ITM", ItmConverter.format(LatLonConverter.latLonToItm(latlon))),
            ResultItem("Web Mercator$wbIsClamped", MercatorConverter.format(LatLonConverter.latLonToWebMercator(latlon))),
            ResultItem("ECEF", EcefConverter.format(LatLonConverter.latLonToEcef(latlon)))
        )

    }

    private fun parseAngleToRadians(type: AngleInputType, value: Double): Double {
        try {
            binding.includeForm.tvError.visibility = View.GONE

            return when (type) {
                AngleInputType.DEGREES -> AngleConverter.degreesToRadians(value)
                AngleInputType.RADIANS -> value
                AngleInputType.NATO_MILS -> AngleConverter.milsNatoToRadians(value)
                AngleInputType.ARTILLERY_MILS -> AngleConverter.milsArtilleryToRadians(value)
                AngleInputType.SWEDISH_MILS -> AngleConverter.milsSwedishToRadians(value)
                AngleInputType.GRADIANS -> AngleConverter.gradiansToRadians(value)
                AngleInputType.TURNS -> AngleConverter.turnsToRadians(value)
            }
        } catch (e: Exception) {
            binding.includeForm.tvError.visibility = View.VISIBLE
            binding.includeForm.tvError.text = e.message ?: "Conversion Failed"
            throw e
        }
    }

    private fun formatAllAngleOutputs(rad: Double): List<ResultItem> {
        val deg = AngleConverter.radiansToDegrees(rad)
        /*buildString {
            appendLine("Radians: %.8f".format(rad))
            appendLine("Degrees: %.6f".format(deg))
            appendLine("Gradians (gon): %.6f".format(AngleConverter.radiansToGradians(rad)))
            appendLine("Turns (rev): %.8f".format(AngleConverter.radiansToTurns(rad)))
            appendLine("NATO mils (6400): %.3f".format(AngleConverter.radiansToMilsNato(rad)))
            appendLine("Artillery mils (mil-rad): %.3f".format(AngleConverter.radiansToMilsArtillery(rad)))
            appendLine("Swedish mils (6300): %.3f".format(AngleConverter.radiansToMilsSwedish(rad)))
        }*/

        return listOf(
            ResultItem("Radians", "%.8f".format(rad)),
            ResultItem("Degrees", "%.6f".format(deg)),
            ResultItem("Gradians (gon)", "%.6f".format(AngleConverter.radiansToGradians(rad))),
            ResultItem("Turns (rev)", "%.8f".format(AngleConverter.radiansToTurns(rad))),
            ResultItem("NATO mils (6400)", "%.3f".format(AngleConverter.radiansToMilsNato(rad))),
            ResultItem("Artillery mils (mil-rad)", "%.3f".format(AngleConverter.radiansToMilsArtillery(rad))),
            ResultItem("Swedish mils (6300)", "%.3f".format(AngleConverter.radiansToMilsSwedish(rad)))
        )
    }

    private fun parseDistance(type: DistanceInputType, input: String): List<ResultItem> {
        var meters: Double

        return when (type) {
            DistanceInputType.METERS -> {
                meters = input.toDouble()

                listOf(
                    ResultItem("Meters", "%.3f".format(meters)),
                    ResultItem("Kilometers", "%.6f".format(meters / 1000.0)),
                    ResultItem("Feet", "%.3f".format(meters / 0.3048)),
                    ResultItem("Miles", "%.6f".format(meters / 1609.344)),
                    ResultItem("Nautical miles", "%.6f".format(meters / 1852.0))
                )
            }

            DistanceInputType.KILOMETERS -> {
                val km = input.toDouble()
                meters = km * 1000.0

                listOf(
                    ResultItem("Meters", "%.3f".format(meters)),
                    ResultItem("Kilometers", "%.6f".format(km)),
                    ResultItem("Feet", "%.3f".format(meters / 0.3048)),
                    ResultItem("Miles", "%.6f".format(meters / 1609.344)),
                    ResultItem("Nautical miles", "%.6f".format(meters / 1852.0))
                )
            }

            DistanceInputType.FEET -> {
                val ft = input.toDouble()
                meters = ft * 0.3048

                listOf(
                    ResultItem("Meters", "%.3f".format(meters)),
                    ResultItem("Kilometers", "%.6f".format(meters / 1000.0)),
                    ResultItem("Feet", "%.3f".format(ft)),
                    ResultItem("Miles", "%.6f".format(meters / 1609.344)),
                    ResultItem("Nautical miles", "%.6f".format(meters / 1852.0))
                )
            }

            DistanceInputType.MILES -> {
                val miles = input.toDouble()
                meters = miles * 1609.344

                listOf(
                    ResultItem("Meters", "%.3f".format(meters)),
                    ResultItem("Kilometers", "%.6f".format(meters / 1000.0)),
                    ResultItem("Feet", "%.3f".format(meters / 0.3048)),
                    ResultItem("Miles", "%.6f".format(miles)),
                    ResultItem("Nautical miles", "%.6f".format(meters / 1852.0))
                )
            }

            DistanceInputType.NAUTICAL_MILES -> { // Unit value in meters -> show in other units (simple version)
                val n_miles = input.toDouble()
                meters = n_miles * 1852

                listOf(
                    ResultItem("Meters", "%.3f".format(meters)),
                    ResultItem("Kilometers", "%.6f".format(meters / 1000.0)),
                    ResultItem("Feet", "%.3f".format(meters / 0.3048)),
                    ResultItem("Miles", "%.6f".format(meters / 1609.344)),
                    ResultItem("Nautical miles", "%.6f".format(n_miles))
                )
            }
        }


    }



}
