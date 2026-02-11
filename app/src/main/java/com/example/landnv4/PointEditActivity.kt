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
    override fun getLayoutResId() = R.layout.activity_point_edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = contentContainer.getChildAt(0)
        binding = ActivityPointEditBinding.bind(root)
        //setContentView(binding.root)

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
        //binding.includeForm.formSubtitle.visibility = View.GONE
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
        val easting = intent.getDoubleExtra(EXTRA_UTM_E, 0.0)
        if (easting < 1) return

        formAdapter.state.set("utm_easting", easting.toString())
        formAdapter.state.set("utm_northing", intent.getDoubleExtra(EXTRA_UTM_N, 0.0).toString())
        formAdapter.state.set("utm_zone", intent.getIntExtra(EXTRA_UTM_Z, 0).toString())
        formAdapter.state.set("utm_hemisphere", intent.getBooleanExtra(EXTRA_UTM_H, true))

        formAdapter.state.set("NAME", intent.getStringExtra(EXTRA_NAME) ?: "")
        formAdapter.state.set("LOCATION", intent.getStringExtra(EXTRA_LOCATION) ?: "")

        formAdapter.state.set("height_value", intent.getDoubleExtra(EXTRA_HEIGHT, 0.0).toString())
        formAdapter.state.set("height_unit", intent.getStringExtra(EXTRA_HEIGHT_TYPE) ?: "")

        formAdapter.notifyDataSetChanged()
    }

    private fun save() {
        val name = formAdapter.state.getString("NAME").orEmpty()
        val location = formAdapter.state.getString("LOCATION").orEmpty()

        val utmE = formAdapter.state.getString("utm_easting").orEmpty()
        val utmN = formAdapter.state.getString("utm_northing").orEmpty()
        val utmZone = formAdapter.state.getString("utm_zone").orEmpty()
        val utmHemi = formAdapter.state.getBoolean("utm_hemisphere") ?: true

        val heightStr = formAdapter.state.getString("height_value").orEmpty()
        val heightUnit = toHeightType(formAdapter.state.getString("height_unit") ?: "METERS")
        val height = heightStr.toDoubleOrNull() ?: 0.0


        if (name.isBlank() || location.isBlank() || utmE.isBlank() || utmN.isBlank()
            || utmZone.isBlank() || heightStr.isBlank()) {
            throw IllegalArgumentException("Please fill all fields.")
        }

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
            binding.includeForm.tvError.visibility = View.GONE
            finish()
        } catch (e: Exception){
            binding.includeForm.tvError.visibility = View.VISIBLE
            binding.includeForm.tvError.text = e.message
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
