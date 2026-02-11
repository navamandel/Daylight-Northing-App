package com.example.landnv4

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.landnv4.data.db.infobank.HeightType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.Button
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.key
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import com.example.landnv4.databinding.ActivityDistanceCalculatorBinding
import com.example.landnv4.domain.geo.DistanceCalc.distanceMeters
import com.example.landnv4.databank.BankType
import com.example.landnv4.domain.geo.UtmParser.parseUtm
import com.example.landnv4.ui.databank.PointItem
import kotlin.getValue
import com.example.landnv4.data.inputs.AppInputsStore.load
import com.example.landnv4.databinding.IncludeFormBinding
import com.example.landnv4.databinding.IncludeResultsBinding
import com.example.landnv4.domain.geo.DistanceCalc.distance3D
import com.example.landnv4.domain.geo.DistanceCalc.feetToMeters
import com.example.landnv4.domain.geo.DistanceCalc.metersToFeet
import com.example.landnv4.domain.geo.Utm
import com.example.landnv4.ui.ExpandableSection
import com.example.landnv4.ui.databank.PointsListViewModel
import com.example.landnv4.ui.ResultItem
import com.example.landnv4.ui.ResultsAdapter
import com.example.landnv4.ui.form.FormAdapter
import com.example.landnv4.ui.form.FormItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView


class YeilutActivity : BaseActivity()  {

    private lateinit var binding: ActivityDistanceCalculatorBinding

    private val vm: PointsListViewModel by viewModels()

    override fun getLayoutResId() = R.layout.activity_distance_calculator
    private lateinit var btnCalc: MaterialButton
    private lateinit var btnCont: MaterialButton
    private lateinit var resultsBinding: IncludeResultsBinding
    private lateinit var secR: ExpandableSection
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var formABinding: IncludeFormBinding
    private lateinit var formBBinding: IncludeFormBinding
    private lateinit var secA: ExpandableSection
    private lateinit var secB: ExpandableSection
    private lateinit var formAdapterA: FormAdapter
    private lateinit var formAdapterB: FormAdapter

    private val KEY_SPINNER_A = "spinner_a"

    private val KEY_UTM_A = "utm_a"
    private val KEY_UTM_A_EST = "utm_a_easting"
    private val KEY_UTM_A_NRT = "utm_a_northing"
    private val KEY_UTM_A_ZONE = "utm_a_zone"
    private val KEY_UTM_A_HEMI = "utm_a_hemisphere"

    private val KEY_HEIGHT_A = "height_a"
    private val KEY_HEIGHT_A_VAL = "height_a_value"
    private val KEY_HEIGHT_A_UT = "height_a_unit"

    private val KEY_SPINNER_B = "spinner_b"
    private val KEY_UTM_B = "utm_b"
    private val KEY_UTM_B_EST = "utm_b_easting"
    private val KEY_UTM_B_NRT = "utm_b_northing"
    private val KEY_UTM_B_ZONE = "utm_b_zone"
    private val KEY_UTM_B_HEMI = "utm_b_hemisphere"

    private val KEY_HEIGHT_B = "height_b"
    private val KEY_HEIGHT_B_VAL = "height_b_value"
    private val KEY_HEIGHT_B_UT = "height_b_unit"

    private lateinit var tvErr: TextView

    private var currentUtm: Utm? = null
    private lateinit var utmA: Utm
    private var heightA: Double = 0.0


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = contentContainer.getChildAt(0)
        binding = ActivityDistanceCalculatorBinding.bind(root)

        setupToolbar("Distance Calculator")

        val themedInflater = LayoutInflater.from(
            ContextThemeWrapper(this, R.style.ThemeOverlay_LandN_Form_Card)
        )
        val themedResInflater = LayoutInflater.from(
            ContextThemeWrapper(this, R.style.ThemeOverlay_LandN_Results_A)
        )

        formABinding = IncludeFormBinding.inflate(
            themedInflater,
            binding.sectionPointA.findViewById<FrameLayout>(R.id.content),
            true
        )
        formBBinding = IncludeFormBinding.inflate(
            themedInflater,
            binding.sectionPointB.findViewById<FrameLayout>(R.id.content),
            true
        )
        resultsBinding = IncludeResultsBinding.inflate(
            themedResInflater,
            binding.sectionResults.findViewById<FrameLayout>(R.id.content),
            true
        )


        secA = ExpandableSection(
            root = binding.sectionPointA.findViewById<MaterialCardView>(R.id.card),
            header = binding.sectionPointA.findViewById<LinearLayout>(R.id.header),
            content = binding.sectionPointA.findViewById<FrameLayout>(R.id.content),
            btnToggle = binding.sectionPointA.findViewById(R.id.btnToggle),
            btnClear = binding.sectionPointA.findViewById(R.id.btnClear),
            btnAction = binding.sectionPointA.findViewById(R.id.btnAction)
        ).apply {
            setTitle("Point A")
            setActionVisible("Continue")
            setExpanded(true, false)
            wireClicks()
        }

        btnCont = binding.sectionPointA.findViewById(R.id.btnAction)

        secB = ExpandableSection(
            root = binding.sectionPointB.findViewById<MaterialCardView>(R.id.card),
            header = binding.sectionPointB.findViewById<LinearLayout>(R.id.header),
            content = binding.sectionPointB.findViewById<FrameLayout>(R.id.content),
            btnToggle = binding.sectionPointB.findViewById(R.id.btnToggle),
            btnClear = binding.sectionPointB.findViewById(R.id.btnClear),
            btnAction = binding.sectionPointB.findViewById(R.id.btnAction)
        ).apply {
            setTitle("Point B")
            setActionVisible("Calculate")
            setExpanded(false, false)
            wireClicks()
        }

        btnCalc = binding.sectionPointB.findViewById(R.id.btnAction)

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

        val storedInfo = load(this)
        currentUtm = storedInfo?.utm13

        val pointsSpOptions = if (currentUtm != null) {
            listOf(
                "initial" to "Select point type…",
                "curr_utm" to "Current Utm",
                BankType.ANCHORING.name to BankType.ANCHORING.title(),
                BankType.NORTHING.name to BankType.NORTHING.title(),
                BankType.VALIDATING.name to BankType.VALIDATING.title(),
                BankType.TARGETS.name to BankType.TARGETS.title()
            )
        } else {
            listOf(
                "initial" to "Select point type…",
                BankType.ANCHORING.name to BankType.ANCHORING.title(),
                BankType.NORTHING.name to BankType.NORTHING.title(),
                BankType.VALIDATING.name to BankType.VALIDATING.title(),
                BankType.TARGETS.name to BankType.TARGETS.title()
            )
        }

        formAdapterA = FormAdapter(listOf(
            FormItem.Spinner(
                key = KEY_SPINNER_A,
                label = "Choose from stored points",
                staticOptions = pointsSpOptions,
                onItemChanged = {
                    val selected = formAdapterA.state.getString(KEY_SPINNER_A)
                    //val offset = if (pointsSpOptions.size == 5) 1 else 2

                    when(selected) {
                        "initial" -> return@Spinner
                        "curr_utm" -> {
                            formAdapterA.state.set(KEY_UTM_A_EST, currentUtm?.easting?.toString().orEmpty())
                            formAdapterA.state.set(KEY_UTM_A_NRT, currentUtm?.northing?.toString().orEmpty())
                            formAdapterA.state.set(KEY_UTM_A_ZONE, currentUtm?.zone?.toString().orEmpty())
                            formAdapterA.state.set(KEY_UTM_A_HEMI, currentUtm?.hemisphereNorth)

                            formAdapterA.state.set(KEY_HEIGHT_A_VAL, "0.0")
                            formAdapterA.state.set(KEY_HEIGHT_A_UT, "METERS")

                            //formAdapterA.state.set("show_current", true)
                            formAdapterA.notifyDataSetChanged()
                        }
                        else -> {
                            try {
                                formABinding.tvError.visibility = View.GONE
                                openPickPointDialog(BankType.valueOf(selected.toString()), true)
                            } catch (e: Exception) {
                                formABinding.tvError.visibility = View.VISIBLE
                                formABinding.tvError.text = e.message
                            }
                        }
                    }
                }
            ),
            FormItem.UtmItem(
                key = KEY_UTM_A,
                label = "Utm A",
                eastingKey = KEY_UTM_A_EST,
                northingKey = KEY_UTM_A_NRT,
                zoneKey = KEY_UTM_A_ZONE,
                hemisphereKey = KEY_UTM_A_HEMI
            ),
            FormItem.Height(
                key = KEY_HEIGHT_A,
                label = "Height A",
                valueKey = KEY_HEIGHT_A_VAL,
                unitKey = KEY_HEIGHT_A_UT
            )
        ))
        formABinding.formTitle.visibility = View.GONE
        formABinding.rvInput.adapter = formAdapterA
        formABinding.rvInput.layoutManager = LinearLayoutManager(this)

        formAdapterB = FormAdapter(listOf(
            FormItem.Spinner(
                key = KEY_SPINNER_B,
                label = "Choose from stored points",
                staticOptions = pointsSpOptions,
                onItemChanged = {
                    val selected = formAdapterB.state.getString(KEY_SPINNER_B)
                    //val offset = if (pointsSpOptions.size == 5) 1 else 2

                    when(selected) {
                        "initial" -> return@Spinner
                        "curr_utm" -> {
                            formAdapterB.state.set(KEY_UTM_B_EST, currentUtm?.easting?.toString().orEmpty())
                            formAdapterB.state.set(KEY_UTM_B_NRT, currentUtm?.northing?.toString().orEmpty())
                            formAdapterB.state.set(KEY_UTM_B_ZONE, currentUtm?.zone?.toString().orEmpty())
                            formAdapterB.state.set(KEY_UTM_B_HEMI, currentUtm?.hemisphereNorth)

                            formAdapterB.state.set(KEY_HEIGHT_B_VAL, "0.0")
                            formAdapterB.state.set(KEY_HEIGHT_B_UT, "METERS")

                            //formAdapterB.state.set("show_current", true)
                            formAdapterB.notifyDataSetChanged()
                        }
                        else -> {
                            try {
                                formBBinding.tvError.visibility = View.GONE
                                openPickPointDialog(BankType.valueOf(selected.toString()), false)
                            } catch (e: Exception) {
                                formBBinding.tvError.visibility = View.VISIBLE
                                formBBinding.tvError.text = e.message
                            }
                        }
                    }
                }
            ),
            FormItem.UtmItem(
                key = KEY_UTM_B,
                label = "Utm B",
                eastingKey = KEY_UTM_B_EST,
                northingKey = KEY_UTM_B_NRT,
                zoneKey = KEY_UTM_B_ZONE,
                hemisphereKey = KEY_UTM_B_HEMI
            ),
            FormItem.Height(
                key = KEY_HEIGHT_B,
                label = "Height B",
                valueKey = KEY_HEIGHT_B_VAL,
                unitKey = KEY_HEIGHT_B_UT
            )
        ))
        formBBinding.formTitle.visibility = View.GONE
        formBBinding.rvInput.adapter = formAdapterB
        formBBinding.rvInput.layoutManager = LinearLayoutManager(this)

        resultsAdapter = ResultsAdapter()

        resultsBinding.rvResults.adapter = resultsAdapter
        resultsBinding.rvResults.layoutManager = LinearLayoutManager(this)
        resultsBinding.rvResults.isNestedScrollingEnabled = false
        resultsBinding.tvResultsTitle.visibility = View.GONE

        makeCardFlat(listOf(
            formABinding.formRoot,
            formBBinding.formRoot,
            resultsBinding.resultsRoot
        ))

        //tvErr = findViewById(R.id.tvError)

        /*formAdapterA.state.addListener { key ->
            if (key == KEY_SPINNER_A) {
                val selected = formAdapterA.state.getString(KEY_SPINNER_A)
                //val offset = if (pointsSpOptions.size == 5) 1 else 2

                when(selected) {
                    "initial" -> return@addListener
                    "curr_utm" -> {
                        formAdapterA.state.set(KEY_UTM_A_EST, currentUtm?.easting?.toString().orEmpty())
                        formAdapterA.state.set(KEY_UTM_A_NRT, currentUtm?.northing?.toString().orEmpty())
                        formAdapterA.state.set(KEY_UTM_A_ZONE, currentUtm?.zone?.toString().orEmpty())
                        formAdapterA.state.set(KEY_UTM_A_HEMI, currentUtm?.hemisphereNorth)

                        formAdapterA.state.set(KEY_HEIGHT_A_VAL, "0.0")
                        formAdapterA.state.set(KEY_HEIGHT_A_UT, "METERS")

                        //formAdapterA.state.set("show_current", true)
                        formAdapterA.notifyDataSetChanged()
                    }
                    else -> {
                        try {
                            openPickPointDialog(BankType.valueOf(selected.toString()), true)
                        } catch (e: Exception) {
                            tvErr.text = e.message
                        }
                    }
                }
            }
        }

        formAdapterB.state.addListener { key ->
            if (key == KEY_SPINNER_B) {
                val selected = formAdapterB.state.getString(KEY_SPINNER_B)
                //val offset = if (pointsSpOptions.size == 5) 1 else 2

                when(selected) {
                    "initial" -> return@addListener
                    "curr_utm" -> {
                        formAdapterB.state.set(KEY_UTM_B_EST, currentUtm?.easting?.toString().orEmpty())
                        formAdapterB.state.set(KEY_UTM_B_NRT, currentUtm?.northing?.toString().orEmpty())
                        formAdapterB.state.set(KEY_UTM_B_ZONE, currentUtm?.zone?.toString().orEmpty())
                        formAdapterB.state.set(KEY_UTM_B_HEMI, currentUtm?.hemisphereNorth)

                        formAdapterB.state.set(KEY_HEIGHT_B_VAL, "0.0")
                        formAdapterB.state.set(KEY_HEIGHT_B_UT, "METERS")

                        //formAdapterB.state.set("show_current", true)
                        formAdapterB.notifyDataSetChanged()
                    }
                    else -> {
                        try {
                            openPickPointDialog(BankType.valueOf(selected.toString()), false)
                        } catch (e: Exception) {
                            tvErr.text = e.message
                        }
                    }
                }
            }
        }*/

        setupButtons()
    }

    /*private fun setupPoints() {
        val types = BankType.values().toList()
        val typess = listOf("Select point type…", "Current Utm") +
                BankType.entries.map { it.title() }

        spPointType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            typess.map { it }
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spPointType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (ignoreFirstSelection) { // prevent auto-trigger on first set
                    ignoreFirstSelection = false
                    return
                }

                if (position == 0) return

                val selectedType = types[position - 1]
                openPickPointDialog(selectedType, true)
                spPointType.setSelection(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }*/

    private fun openPickPointDialog(type: BankType, isA: Boolean) {
        lifecycleScope.launch {
            val points = vm.getPointsOnce(type)

            if (points.isEmpty()) {
                AlertDialog.Builder(this@YeilutActivity)
                    .setTitle("No points in ${type.title()}")
                    .setMessage("Add points first, then try again.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            // Display lines in the list
            val display = points.map { p ->
                val name = p.name.ifBlank { "(No name)" }
                "$name  •  UTM: ${p.utm}"
            }.toTypedArray()

            var selectedIndex = -1

            val dialog = AlertDialog.Builder(this@YeilutActivity)
                .setTitle("Choose a point from ${type.title()}")
                .setSingleChoiceItems(display, -1) { _, which ->
                    selectedIndex = which
                    // enable buttons once a selection exists (we do this in onShow below)
                }
                .setNeutralButton("Cancel", null)   // we override click later
                .setPositiveButton("Set Point ${if(isA) "A" else "B"}", null)  // override click later
                .create()

            dialog.setOnShowListener {
                val btn: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                fun updateButtons() {
                    val enabled = selectedIndex != -1
                    btn.isEnabled = enabled
                }

                updateButtons()

                // When selection changes, update button state:
                // AlertDialog doesn't auto-call updateButtons, so we can hook into list view:
                dialog.listView.setOnItemClickListener { _, _, which, _ ->
                    selectedIndex = which
                    updateButtons()
                }

                btn.setOnClickListener {
                    if (selectedIndex == -1) return@setOnClickListener
                    val chosen = points[selectedIndex]

                    if (isA) {
                        formAdapterA.state.set(KEY_UTM_A_EST, chosen.utm.easting.toString())
                        formAdapterA.state.set(KEY_UTM_A_NRT, chosen.utm.northing.toString())
                        formAdapterA.state.set(KEY_UTM_A_ZONE, chosen.utm.zone.toString())
                        formAdapterA.state.set(KEY_UTM_A_HEMI, chosen.utm.hemisphereNorth)

                        formAdapterA.state.set(KEY_HEIGHT_A_VAL, chosen.height.toString())
                        formAdapterA.state.set(KEY_HEIGHT_A_UT, chosen.heightType.name)

                        formAdapterA.notifyDataSetChanged()
                    } else {
                        formAdapterB.state.set(KEY_UTM_B_EST, chosen.utm.easting.toString())
                        formAdapterB.state.set(KEY_UTM_B_NRT, chosen.utm.northing.toString())
                        formAdapterB.state.set(KEY_UTM_B_ZONE, chosen.utm.zone.toString())
                        formAdapterB.state.set(KEY_UTM_B_HEMI, chosen.utm.hemisphereNorth)

                        formAdapterB.state.set(KEY_HEIGHT_B_VAL, chosen.height.toString())
                        formAdapterB.state.set(KEY_HEIGHT_B_UT, chosen.heightType.name)

                        formAdapterB.notifyDataSetChanged()
                    }


                    //etUtmB.setText(chosen.utm.toString())
                    //etHeightB.setText(chosen.height.toString())
                    //spUnitB.setSelection(if (chosen.heightType == HeightType.METERS) 0 else 1)
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }


    /*private fun setupUseCurrentA() {
        val hasCurrent = currentUtm != null

        formAdapterA.state.setEnabled(KEY_SWITCH, hasCurrent)

        formAdapterA.state.addListener { key ->
            run {
                if (formAdapterA.state.isEnabled(key) && key == KEY_SWITCH) {
                    val checked = formAdapterA.state.getBoolean(key) ?: false
                    if (checked) {
                        formAdapterA.state.set(KEY_UTM_A_EST, currentUtm?.easting?.toString().orEmpty())
                        formAdapterA.state.set(KEY_UTM_A_NRT, currentUtm?.northing?.toString().orEmpty())
                        formAdapterA.state.set(KEY_UTM_A_ZONE, currentUtm?.zone?.toString().orEmpty())
                        formAdapterA.state.set(KEY_UTM_A_HEMI, currentUtm?.hemisphereNorth)

                        formAdapterA.state.set("show_current", true)
                        formAdapterA.notifyDataSetChanged()

                    } else if (formAdapterA.state.getBoolean("show_current") ?: false) {
                        formAdapterA.state.clearValue(KEY_UTM_A_EST)
                        formAdapterA.state.clearValue(KEY_UTM_A_NRT)
                        formAdapterA.state.clearValue(KEY_UTM_A_ZONE)
                        formAdapterA.state.clearValue(KEY_UTM_A_HEMI)

                        formAdapterA.state.set("show_current", false)
                        formAdapterA.notifyDataSetChanged()
                    }

                    formAdapterA.state.setEnabled(KEY_UTM_A, !checked)
                    formAdapterA.safeNotifyItemChanged(0, FormAdapter.Payload.ENABLED)
                }
            }
        }
    }*/




    /*private fun startTargetsCollectionn() {
        targetsJob?.cancel()
        targetsJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pointsFlow(BankType.TARGETS).collect { list ->
                    targets = list

                    if (list.isEmpty()) {
                        spTargetB.adapter = ArrayAdapter(
                            this@YeilutActivity,
                            android.R.layout.simple_spinner_item,
                            listOf("No targets saved")
                        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                        return@collect
                    }

                    // display as "Name (UTM)"
                    val labels = list.map { "${it.name} (UTM: ${it.utm})" }
                    spTargetB.adapter = ArrayAdapter(
                        this@YeilutActivity,
                        android.R.layout.simple_spinner_item,
                        labels
                    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                    if (swUseTargetB.isChecked) {
                        spTargetB.setSelection(0)
                        fillBFromTarget(list[0])
                    }
                }
            }
        }
    }

    private fun startTargetsCollection() {
        targetsJob?.cancel()
        targetsJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pointsFlow(BankType.TARGETS).collect { list ->
                    targets = list

                    if (list.isEmpty()) {
                        spTargetB.visibility = View.GONE
                        // keep manual entry available
                        setBEditable(true)
                        return@collect
                    }

                    // Show spinner and populate it
                    spTargetB.visibility = View.VISIBLE

                    val labels = list.map { "${it.name} (UTM: ${it.utm})" }
                    spTargetB.adapter = ArrayAdapter(
                        this@YeilutActivity,
                        android.R.layout.simple_spinner_item,
                        labels
                    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                }
            }
        }
    }*/


    private fun setupButtons() {
        var isSetA = false
        btnCont.setOnClickListener {
            setPointA(formABinding.tvError)
            isSetA = true
        }

        btnCalc.setOnClickListener {
            val tvErrB = formBBinding.tvError
            var utmB: Utm
            var heightB: Double

            try {
                if (!isSetA) setPointA(formABinding.tvError)

                tvErrB.visibility = View.VISIBLE
                tvErrB.text = ""

                val eB = formAdapterB.state.getString(KEY_UTM_B_EST)
                    ?.trim()
                    ?: throw IllegalArgumentException("Missing easting")

                val nB = formAdapterB.state.getString(KEY_UTM_B_NRT)
                    ?.trim()
                    ?: throw IllegalArgumentException("Missing northing")

                val zB = formAdapterB.state.getString(KEY_UTM_B_ZONE)
                    ?.trim()
                    ?: throw IllegalArgumentException("Missing zone")

                val hB = formAdapterB.state.getBoolean(KEY_UTM_B_HEMI) ?: true

                utmB = parseUtm(eB, nB, zB, hB)

                val heighttB = formAdapterB.state.getString(KEY_HEIGHT_B_VAL)
                    ?.trim()
                    ?.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Missing/invalid height")
                val unitB = formAdapterB.state.getString(KEY_HEIGHT_B_UT) ?: "METERS"

                val uB = HeightType.valueOf(unitB)

                heightB = if (uB == HeightType.FEET) feetToMeters(heighttB) else heighttB

                tvErrB.visibility = View.GONE

                try {
                    val d2d = distanceMeters(utmA, utmB)
                    val d3d = distance3D(utmA, utmB, heightB - heightA)

                    resultsAdapter.submitList(
                        listOf(
                            ResultItem("2D (ground)", "%.2f m, %.2f ft".format(d2d, metersToFeet(d2d))),
                            ResultItem("3D (with height)", "%.2f m, %.2f ft".format(d3d, metersToFeet(d3d))),
                        )
                    )
                    applyEffectivenessUI(d3d)

                    secB.setExpanded(false)

                    resultsBinding.root.visibility = View.VISIBLE
                    resultsBinding.resultsContainer.visibility = View.VISIBLE
                    binding.sectionResults.visibility = View.VISIBLE
                } catch (e: Exception) {

                    tvErr.text = e.message ?: "Calculation failed"
                }

            } catch (e: Exception) {
                tvErrB.text = e.message ?: "Invalid UTM Coordinates"
            }


        }


        /*btnCalc.setOnClickListener {
            tvErr.text = ""

            try {

                val utm_a = parseUtm(
                    formAdapterA.state.getString(KEY_UTM_A_EST)!!,
                    formAdapterA.state.getString(KEY_UTM_A_NRT)!!,
                    formAdapterA.state.getString(KEY_UTM_A_ZONE)!!.toInt(),
                    formAdapterA.state.getBoolean(KEY_UTM_A_HEMI)!!,
                )
                val utm_b = parseUtm(
                    formAdapterB.state.getString(KEY_UTM_B_EST)!!,
                    formAdapterB.state.getString(KEY_UTM_B_NRT)!!,
                    formAdapterB.state.getString(KEY_UTM_B_ZONE)!!.toInt(),
                    formAdapterB.state.getBoolean(KEY_UTM_B_HEMI)!!,
                )

                val d2d = distanceMeters(utm_a, utm_b)

                val hA = formAdapterA.state.getString(KEY_HEIGHT_A_VAL)!!.toDouble()
                val unitA = HeightType.valueOf(formAdapterA.state.getString(KEY_HEIGHT_A_UT)!!)

                val hB = formAdapterB.state.getString(KEY_HEIGHT_B_VAL)!!.toDouble()
                val unitB = HeightType.valueOf(formAdapterB.state.getString(KEY_HEIGHT_B_UT)!!)

                val zA = if (unitA == HeightType.FEET) feetToMeters(hA) else hA
                val zB = if (unitB == HeightType.FEET) feetToMeters(hB) else hB
                val dz = zB - zA


                val d3d = distance3D(utm_a, utm_b, dz)

                resultsAdapter.submitList(listOf(
                    ResultItem("2D (ground)", "${"%.2f".format(d2d)} m, " +
                            "${"%.2f".format(metersToFeet(d2d))} ft"),
                    ResultItem("3D (with height)", "${"%.2f".format(d3d)} m, " +
                            "${"%.2f".format(metersToFeet(d3d))} ft"))
                )
                applyEffectivenessUI(d3d)
            } catch (e: Exception) {
                binding.tvError.text = e.message
            }
        }*/
    }

    private fun setPointA(tvErrA: TextView) {
        tvErrA.visibility = View.VISIBLE
        tvErrA.text = ""

        try {
            val eA = formAdapterA.state.getString(KEY_UTM_A_EST)
                ?.trim()
                ?: throw IllegalArgumentException("Missing easting")

            val nA = formAdapterA.state.getString(KEY_UTM_A_NRT)
                ?.trim()
                ?: throw IllegalArgumentException("Missing northing")

            val zA = formAdapterA.state.getString(KEY_UTM_A_ZONE)
                ?.trim()
                ?: throw IllegalArgumentException("Missing zone")

            val hA = formAdapterA.state.getBoolean(KEY_UTM_A_HEMI) ?: true

            utmA = parseUtm(eA, nA, zA, hA)

            val heighttA = formAdapterA.state.getString(KEY_HEIGHT_A_VAL)
                ?.trim()
                ?.toDoubleOrNull()
                ?: throw IllegalArgumentException("Missing/invalid height")
            val unitA = formAdapterA.state.getString(KEY_HEIGHT_A_UT) ?: "METERS"
            val uA = HeightType.valueOf(unitA)

            heightA = if (uA == HeightType.FEET) feetToMeters(heighttA) else heighttA

            tvErrA.visibility = View.GONE
            secA.setExpanded(false)
            secB.setExpanded(true)

        } catch (e: Exception) {
            tvErrA.visibility = View.VISIBLE
            tvErrA.text = e.message ?: "Invalid UTM Coordinates"

        }
    }


    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun applyEffectivenessUI(distanceMeters: Double) {
        val effective = distanceMeters < 20005.0

        val okColor = Color.parseColor("#2E7D32")   // green
        val badColor = Color.parseColor("#C62828")  // red
        val color = if (effective) okColor else badColor
        val km = distanceMeters / 1000.0

        resultsBinding.tvResultsTitle.visibility = View.VISIBLE
        resultsBinding.tvResultsTitle.text = if (effective) {
            "✅ EFFECTIVE (${String.format("%.3f", km)} km < 20.005)"
        } else {
            "❌ NOT EFFECTIVE (${String.format("%.3f", km)} km ≥ 20.005)"
        }
        resultsBinding.tvResultsTitle.setTextColor(color)

    }

    fun makeCardFlat(cards: List<MaterialCardView>) {
        cards.forEach { card -> card.apply {
            strokeWidth = 0
            strokeColor = Color.TRANSPARENT

            cardElevation = 0f
            elevation = 0f

            // removes extra shadow padding
            useCompatPadding = false
            preventCornerOverlap = false
        } }

    }



}

/*
package com.example.landnv4

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.landnv4.data.db.infobank.HeightType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import android.app.AlertDialog
import android.widget.AdapterView
import android.widget.Button
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import com.example.landnv4.databinding.ActivityDistanceCalculatorBinding
import com.example.landnv4.domain.geo.DistanceCalc.distanceMeters
import com.example.landnv4.databank.BankType
import com.example.landnv4.domain.geo.UtmParser.parseUtm
import com.example.landnv4.ui.databank.PointItem
import kotlin.getValue
import com.example.landnv4.data.inputs.AppInputsStore.load
import com.example.landnv4.databinding.ActivityConverterBinding
import com.example.landnv4.domain.geo.DistanceCalc.distance3D
import com.example.landnv4.domain.geo.DistanceCalc.feetToMeters
import com.example.landnv4.domain.geo.DistanceCalc.metersToFeet
import com.example.landnv4.domain.geo.Utm
import com.example.landnv4.ui.databank.PointsListViewModel
import com.example.landnv4.ui.ResultItem
import com.example.landnv4.ui.ResultsAdapter
import com.example.landnv4.ui.form.FormAdapter


class YeilutActivity : BaseActivity()  {
    companion object {
        const val EXTRA_CURRENT_UTM = "extra_current_utm"
        const val EXTRA_CURRENT_HEIGHT = "extra_current_height"
        const val EXTRA_CURRENT_HEIGHT_TYPE = "extra_current_height_type" // "METERS" / "FEET"
    }

    private lateinit var binding: ActivityDistanceCalculatorBinding

    private val vm: PointsListViewModel by viewModels()

    // A
    private lateinit var swUseCurrentA: com.google.android.material.switchmaterial.SwitchMaterial
    private lateinit var etUtmA: com.google.android.material.textfield.TextInputEditText
    private lateinit var etHeightA: com.google.android.material.textfield.TextInputEditText
    private lateinit var spUnitA: Spinner

    // B
    private lateinit var swUseTargetB: com.google.android.material.switchmaterial.SwitchMaterial
    private lateinit var spPointType: Spinner
    private lateinit var tilUtmB: com.google.android.material.textfield.TextInputLayout
    private lateinit var etUtmB: com.google.android.material.textfield.TextInputEditText
    private lateinit var etHeightB: com.google.android.material.textfield.TextInputEditText
    private lateinit var spUnitB: Spinner

    private lateinit var btnSwap: Button
    private lateinit var btnCalc: Button
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var formAdapter: FormAdapter

    private lateinit var tvErr: TextView

    private var ignoreFirstSelection = true

    private var currentUtm: Utm? = null
    private lateinit var tvStatus: TextView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDistanceCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupToolbar("יעילות")

        // Bind
        swUseCurrentA = binding.swUseCurrentA
        etUtmA = binding.etUtmA
        etHeightA = binding.etHeightA
        spUnitA = binding.spHeightUnitA

        spPointType = binding.spPointType
        tilUtmB = binding.tilUtmB
        etUtmB = binding.etUtmB
        etHeightB = binding.etHeightB
        spUnitB = binding.spHeightUnitB

        // btnSwap = findViewById(R.id.btnSwap)
        btnCalc = binding.btnCalculate

        resultsAdapter = ResultsAdapter()

        binding.includeResults.rvResults.adapter = resultsAdapter
        binding.includeResults.rvResults.layoutManager = LinearLayoutManager(this)
        binding.includeResults.rvResults.isNestedScrollingEnabled = false

        tvErr = findViewById(R.id.tvError)

        // Units spinners
        val unitLabels = listOf("Meters", "Feet")
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitLabels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spUnitA.adapter = unitAdapter
        spUnitB.adapter = unitAdapter
        spUnitA.setSelection(0)
        spUnitB.setSelection(0)

        val storedInfo = load(this)
        currentUtm = storedInfo?.utm13

        setupPoints()
        setupUseCurrentA()
        setupButtons()
    }

    private fun setupPoints() {
        val types = BankType.values().toList()
        val typess = listOf("Select point type…") + BankType.values().map { it.title() }

        spPointType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            typess.map { it }
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spPointType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (ignoreFirstSelection) { // prevent auto-trigger on first set
                    ignoreFirstSelection = false
                    return
                }

                if (position == 0) return

                val selectedType = types[position - 1]
                openPickPointDialog(selectedType)
                spPointType.setSelection(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun openPickPointDialog(type: BankType) {
        lifecycleScope.launch {
            val points = vm.getPointsOnce(type)

            if (points.isEmpty()) {
                AlertDialog.Builder(this@YeilutActivity)
                    .setTitle("No points in ${type.title()}")
                    .setMessage("Add points first, then try again.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            // Display lines in the list
            val display = points.map { p ->
                val name = p.name.ifBlank { "(No name)" }
                "$name  •  UTM: ${p.utm}"
            }.toTypedArray()

            var selectedIndex = -1

            val dialog = AlertDialog.Builder(this@YeilutActivity)
                .setTitle("Choose a point from ${type.title()}")
                .setSingleChoiceItems(display, -1) { _, which ->
                    selectedIndex = which
                    // enable buttons once a selection exists (we do this in onShow below)
                }
                .setNegativeButton("Set Point A", null)
                .setNeutralButton("Cancel", null)   // we override click later
                .setPositiveButton("Set Point B", null)  // override click later
                .create()

            dialog.setOnShowListener {
                val btnA: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                val btnB: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                fun updateButtons() {
                    val enabled = selectedIndex != -1
                    btnA.isEnabled = enabled
                    btnB.isEnabled = enabled
                }

                updateButtons()

                // When selection changes, update button state:
                // AlertDialog doesn't auto-call updateButtons, so we can hook into list view:
                dialog.listView.setOnItemClickListener { _, _, which, _ ->
                    selectedIndex = which
                    updateButtons()
                }

                btnA.setOnClickListener {
                    if (selectedIndex == -1) return@setOnClickListener
                    val chosen = points[selectedIndex]
                    etUtmA.setText(chosen.utm.toString())
                    etHeightA.setText(chosen.height.toString())
                    spUnitA.setSelection(if (chosen.heightType == HeightType.METERS) 0 else 1)
                    dialog.dismiss()
                }

                btnB.setOnClickListener {
                    if (selectedIndex == -1) return@setOnClickListener
                    val chosen = points[selectedIndex]
                    etUtmB.setText(chosen.utm.toString())
                    etHeightB.setText(chosen.height.toString())
                    spUnitB.setSelection(if (chosen.heightType == HeightType.METERS) 0 else 1)
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }


    private fun setupUseCurrentA() {
        val hasCurrent = currentUtm != null
        swUseCurrentA.isEnabled = hasCurrent
        swUseCurrentA.isChecked = false
        etUtmA.isEnabled = true

        if (!hasCurrent) {
            swUseCurrentA.isEnabled = false
            swUseCurrentA.isChecked = false
            swUseCurrentA.text = "Use current UTM from Required Inputs (not available)"
            setAEditable(true)
            return
        }


        swUseCurrentA.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                etUtmA.setText(currentUtm!!)
            } else {
                etUtmA.setText("")
            }
        }
    }


    private fun setAEditable(editable: Boolean) {
        etUtmA.isEnabled = editable

    }



    /*private fun startTargetsCollectionn() {
        targetsJob?.cancel()
        targetsJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pointsFlow(BankType.TARGETS).collect { list ->
                    targets = list

                    if (list.isEmpty()) {
                        spTargetB.adapter = ArrayAdapter(
                            this@YeilutActivity,
                            android.R.layout.simple_spinner_item,
                            listOf("No targets saved")
                        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                        return@collect
                    }

                    // display as "Name (UTM)"
                    val labels = list.map { "${it.name} (UTM: ${it.utm})" }
                    spTargetB.adapter = ArrayAdapter(
                        this@YeilutActivity,
                        android.R.layout.simple_spinner_item,
                        labels
                    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                    if (swUseTargetB.isChecked) {
                        spTargetB.setSelection(0)
                        fillBFromTarget(list[0])
                    }
                }
            }
        }
    }

    private fun startTargetsCollection() {
        targetsJob?.cancel()
        targetsJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pointsFlow(BankType.TARGETS).collect { list ->
                    targets = list

                    if (list.isEmpty()) {
                        spTargetB.visibility = View.GONE
                        // keep manual entry available
                        setBEditable(true)
                        return@collect
                    }

                    // Show spinner and populate it
                    spTargetB.visibility = View.VISIBLE

                    val labels = list.map { "${it.name} (UTM: ${it.utm})" }
                    spTargetB.adapter = ArrayAdapter(
                        this@YeilutActivity,
                        android.R.layout.simple_spinner_item,
                        labels
                    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                }
            }
        }
    }*/


    private fun setupButtons() {

        btnCalc.setOnClickListener {
            tvErr.text = ""

            val utmA = etUtmA.text?.toString().orEmpty().trim()
            val utmB = etUtmB.text?.toString().orEmpty().trim()
            if (utmA.isBlank() || utmB.isBlank()) {
                tvErr.text = "Please enter both UTM values."
                return@setOnClickListener
            }

            val hA = etHeightA.text?.toString().orEmpty().trim().toDoubleOrNull()
            val hB = etHeightB.text?.toString().orEmpty().trim().toDoubleOrNull()
            if (hA == null || hB == null) {
                tvErr.text = "Please enter valid heights for both points."
                return@setOnClickListener
            }

            val unitA = if (spUnitA.selectedItemPosition == 1) HeightType.FEET else HeightType.METERS
            val unitB = if (spUnitB.selectedItemPosition == 1) HeightType.FEET else HeightType.METERS

            try {
                val utm_a = parseUtm(utmA)
                val utm_b = parseUtm(utmB)

                val d2d = distanceMeters(utm_a, utm_b)

                val zA = if (unitA == HeightType.FEET) feetToMeters(hA) else hA
                val zB = if (unitB == HeightType.FEET) feetToMeters(hB) else hB
                val dz = zB - zA


                val d3d = distance3D(utm_a, utm_b, dz)

                resultsAdapter.submitList(listOf(
                    ResultItem("2D (ground)", "${"%.2f".format(d2d)} m, " +
                            "${"%.2f".format(metersToFeet(d2d))} ft"),
                    ResultItem("3D (with height)", "${"%.2f".format(d3d)} m, " +
                            "${"%.2f".format(metersToFeet(d3d))} ft"))
                )
                applyEffectivenessUI(d3d)
            } catch (e: Exception) {
                resultsAdapter.submitList(listOf(ResultItem("Error", e.message ?: "Unknown error")))
            }
        }
    }


    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun applyEffectivenessUI(distanceMeters: Double) {
        val effective = distanceMeters < 20005.0

        val okColor = android.graphics.Color.parseColor("#2E7D32")   // green
        val badColor = android.graphics.Color.parseColor("#C62828")  // red
        val color = if (effective) okColor else badColor
        val km = distanceMeters / 1000.0

        binding.includeResults.tvResultsTitle.text = if (effective) {
            "✅ EFFECTIVE (${String.format("%.3f", km)} km < 20.005)"
        } else {
            "❌ NOT EFFECTIVE (${String.format("%.3f", km)} km ≥ 20.005)"
        }
        binding.includeResults.tvResultsTitle.setTextColor(color)


    }



}

 */
