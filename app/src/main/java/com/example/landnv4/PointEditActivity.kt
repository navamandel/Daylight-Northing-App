package com.example.landnv4

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landnv4.data.db.infobank.HeightConverters.toHeightType
import com.example.landnv4.data.db.infobank.HeightType
import com.example.landnv4.databank.BankType
import com.example.landnv4.databinding.ActivityPointEditBinding
import com.example.landnv4.domain.geo.Utm
import com.example.landnv4.ui.databank.PointItem
import com.example.landnv4.domain.geo.UtmParser.parseUtm
import com.example.landnv4.ui.databank.PointsListViewModel
import com.example.landnv4.ui.inputs.RequiredInputsDialog
import com.example.landnv4.ui.form.FormAdapter
import com.example.landnv4.ui.form.FormItem
import com.example.landnv4.ui.form.FormState
import java.lang.Exception

class PointEditActivity : BaseActivity() {

    companion object {
        const val EXTRA_BANK_TYPE = "bank_type"
        const val EXTRA_ID = "id"
        const val EXTRA_UTM_E = "utm_easting"
        const val EXTRA_UTM_N = "utm_northing"
        const val EXTRA_UTM_Z = "utm_zone"
        const val EXTRA_UTM_H = "utm_hemisphere"
        const val EXTRA_LOCATION = "location"
        const val EXTRA_HEIGHT = "height"
        const val EXTRA_HEIGHT_TYPE = "height_type"
        const val EXTRA_NAME = "name"
    }

    private lateinit var binding: ActivityPointEditBinding
    private val vm: PointsListViewModel by viewModels()

    private lateinit var type: BankType
    private var id: Long = 0L

    private lateinit var formAdapter: FormAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPointEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = BankType.valueOf(intent.getStringExtra(EXTRA_BANK_TYPE) ?: BankType.ANCHORING.name)
        id = intent.getLongExtra(EXTRA_ID, 0L)

        setupToolbar("New ${type.title()}")



        val items = listOf(
            FormItem.Text("NAME", "Name"),
            FormItem.UtmItem("UTM", "Utm Coordinates"),
            FormItem.Text("LOCATION", "Location"),
            FormItem.Height("HEIGHT", "Height")
        )

        formAdapter = FormAdapter(items)
        binding.includeForm.formTitle.text = "Add New ${type.title()}"
        binding.includeForm.formSubtitle.visibility = View.GONE
        binding.includeForm.rvInput.apply {
            layoutManager = LinearLayoutManager(this@PointEditActivity)
            adapter = formAdapter
            setHasFixedSize(false)
        }

        setupSpinner()
        prefillIfEditing()

        binding.btnSave.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    override fun onStop() {
        super.onStop()
        formAdapter.state.clearAllValues()
    }


    private fun setupSpinner() {
        /*binding.spHeightType.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.height_type_items,
            android.R.layout.simple_spinner_dropdown_item
        )*/
    }

    private fun prefillIfEditing() {

        /*binding.etName.setText(intent.getStringExtra(EXTRA_NAME) ?: "")
        binding.etUtm.setText(intent.getIntExtra(EXTRA_UTM, 0).toString())
        binding.etLocation.setText(intent.getStringExtra(EXTRA_LOCATION) ?: "")
        binding.etHeight.setText(intent.getIntExtra(EXTRA_HEIGHT, 0).toString())

        val ht = intent.getStringExtra(EXTRA_HEIGHT_TYPE) ?: HeightType.METERS.name
        binding.spHeightType.setSelection(if (ht == HeightType.FEET.name) 1 else 0)*/
    }

    private fun save() {
        val name = formAdapter.state.getString("NAME").toString()
            //binding.etName.text?.toString()?.trim().orEmpty()
        val location = formAdapter.state.getString("LOCATION").toString()
            //binding.etLocation.text?.toString()?.trim().orEmpty()

        val utmE = formAdapter.state.getString("utm_easting").toString()
        val utmN = formAdapter.state.getString("utm_northing").toString()
        val utmZone = formAdapter.state.getString("utm_zone").toString().toInt()
        val utmHemi = formAdapter.state.getBoolean("utm_hemisphere") ?: true
            //binding.etUtm.text?.toString()?.trim().orEmpty()
        val heightStr = formAdapter.state.getString("height_value").toString().toDoubleOrNull()
        val height = heightStr ?: 0.0
        val heightUnit = toHeightType(formAdapter.state.getString("height_unit") ?: "METERS")
            //binding.etHeight.text?.toString()?.trim().orEmpty()

        /*if (name!!.isBlank() || location!!.isBlank() || utmStr.isBlank() || heightStr.isBlank()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }*/

        try {
            val utm = parseUtm(utmE, utmN, utmZone, utmHemi)

            val item = PointItem(
                id = id,
                utm = utm,
                location = location,
                height = height,
                heightType = heightUnit,
                name = name
            )

            if (id == 0L) vm.insert(type, item) else vm.update(type, item)
            formAdapter.state.clearAllValues()
            finish()
        } catch (e: Exception){
            formAdapter.setError("UTM", e.message)
        }


        /*if (utmNum == null || height == null) {
            Toast.makeText(this, "UTM and Height must be integers.", Toast.LENGTH_SHORT).show()
            return
        }*/
        //val heightType = formHeight.second
            //if (binding.spHeightType.selectedItem.toString() == "FEET") HeightType.FEET else HeightType.METERS

        /*val item = PointItem(
            id = id,
            utm = utm,
            location = location,
            height = height,
            heightType = heightType,
            name = name
        )

        if (id == 0L) vm.insert(type, item) else vm.update(type, item)
        finish()*/

        /*startActivity(
            Intent(this, PointsListActivity::class.java)
        )*/
    }
}
