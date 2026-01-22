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
import com.example.landnv4.ui.form.FormItem


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
    private lateinit var formAdapterA: FormAdapter
    private lateinit var formAdapterB: FormAdapter

    private val KEY_SWITCH = "switch"

    private val KEY_UTM_A = "utm_a"
    private val KEY_UTM_A_EST = "utm_a_easting"
    private val KEY_UTM_A_NRT = "utm_a_northing"
    private val KEY_UTM_A_ZONE = "utm_a_zone"
    private val KEY_UTM_A_HEMI = "utm_a_hemisphere"

    private val KEY_HEIGHT_A = "height_a"
    private val KEY_HEIGHT_A_VAL = "height_a_value"
    private val KEY_HEIGHT_A_UT = "height_a_unit"

    private val KEY_UTM_B = "utm_b"
    private val KEY_UTM_B_EST = "utm_b_easting"
    private val KEY_UTM_B_NRT = "utm_b_northing"
    private val KEY_UTM_B_ZONE = "utm_b_zone"
    private val KEY_UTM_B_HEMI = "utm_b_hemisphere"

    private val KEY_HEIGHT_B = "height_b"
    private val KEY_HEIGHT_B_VAL = "height_b_value"
    private val KEY_HEIGHT_B_UT = "height_b_unit"

    private lateinit var tvErr: TextView

    private var ignoreFirstSelection = true

    private var currentUtm: Utm? = null
    private lateinit var tvStatus: TextView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDistanceCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupToolbar("Distance Calculator")

        // Bind
        spPointType = binding.spPointType
        btnCalc = binding.btnCalculate

        formAdapterA = FormAdapter(listOf(
            FormItem.Switch(
                key = KEY_SWITCH,
                label = "Use Current Utm",
                updateKey = "UTM_A"
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
        binding.includeFormA.formTitle.text = "Point A"
        binding.includeFormA.formSubtitle.visibility = View.GONE
        binding.includeFormA.rvInput.adapter = formAdapterA
        binding.includeFormA.rvInput.layoutManager = LinearLayoutManager(this)

        formAdapterB = FormAdapter(listOf(
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
        binding.includeFormB.formTitle.text = "Point B"
        binding.includeFormB.formSubtitle.visibility = View.GONE
        binding.includeFormB.rvInput.adapter = formAdapterB
        binding.includeFormB.rvInput.layoutManager = LinearLayoutManager(this)

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

        //spUnitA.adapter = unitAdapter
        //spUnitB.adapter = unitAdapter
        //spUnitA.setSelection(0)
        //spUnitB.setSelection(0)

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

                    formAdapterA.state.set(KEY_UTM_A_EST, chosen.utm.easting.toString())
                    formAdapterA.state.set(KEY_UTM_A_NRT, chosen.utm.northing.toString())
                    formAdapterA.state.set(KEY_UTM_A_ZONE, chosen.utm.zone)
                    formAdapterA.state.set(KEY_UTM_A_HEMI, chosen.utm.hemisphereNorth)

                    formAdapterA.state.set(KEY_HEIGHT_A_VAL, chosen.height.toString())
                    formAdapterA.state.set(KEY_HEIGHT_A_UT, chosen.heightType.name)

                    //etUtmA.setText(chosen.utm.toString())
                    //etHeightA.setText(chosen.height.toString())
                    //spUnitA.setSelection(if (chosen.heightType == HeightType.METERS) 0 else 1)
                    dialog.dismiss()
                }

                btnB.setOnClickListener {
                    if (selectedIndex == -1) return@setOnClickListener
                    val chosen = points[selectedIndex]

                    formAdapterB.state.set(KEY_UTM_B_EST, chosen.utm.easting.toString())
                    formAdapterB.state.set(KEY_UTM_B_NRT, chosen.utm.northing.toString())
                    formAdapterB.state.set(KEY_UTM_B_ZONE, chosen.utm.zone)
                    formAdapterB.state.set(KEY_UTM_B_HEMI, chosen.utm.hemisphereNorth)

                    formAdapterB.state.set(KEY_HEIGHT_B_VAL, chosen.height.toString())
                    formAdapterB.state.set(KEY_HEIGHT_B_UT, chosen.heightType.name)

                    //etUtmB.setText(chosen.utm.toString())
                    //etHeightB.setText(chosen.height.toString())
                    //spUnitB.setSelection(if (chosen.heightType == HeightType.METERS) 0 else 1)
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }


    private fun setupUseCurrentA() {
        val hasCurrent = currentUtm != null

        formAdapterA.state.setEnabled(KEY_SWITCH, hasCurrent)

        formAdapterA.state.addListener { key ->
            run {
                if (formAdapterA.state.isEnabled(key) && key == KEY_SWITCH) {
                    val checked = formAdapterA.state.getBoolean(key) ?: false
                    if (checked) {
                        formAdapterA.state.set(KEY_UTM_A_EST, currentUtm?.easting.toString())
                        formAdapterA.state.set(KEY_UTM_A_NRT, currentUtm?.northing.toString())
                        formAdapterA.state.set(KEY_UTM_A_ZONE, currentUtm?.zone)
                        formAdapterA.state.set(KEY_UTM_A_HEMI, currentUtm?.hemisphereNorth)

                        formAdapterA.state.set("show_current", true)
                    } else if (formAdapterA.state.getBoolean("show_current") ?: false) {
                        formAdapterA.state.clearValue(KEY_UTM_A_EST)
                        formAdapterA.state.clearValue(KEY_UTM_A_NRT)
                        formAdapterA.state.clearValue(KEY_UTM_A_ZONE)
                        formAdapterA.state.clearValue(KEY_UTM_A_HEMI)

                        formAdapterA.state.set("show_current", false)
                    }

                    formAdapterA.state.setEnabled(KEY_UTM_A_EST, !checked)
                    formAdapterA.state.setEnabled(KEY_UTM_A_NRT, !checked)
                    formAdapterA.state.setEnabled(KEY_UTM_A_ZONE, !checked)
                    formAdapterA.state.setEnabled(KEY_UTM_A_HEMI, !checked)
                }
            }
        }
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
