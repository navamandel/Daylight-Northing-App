package com.example.landnv4

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landnv4.databinding.ActivityHomeBinding
import com.example.landnv4.data.inputs.AppInputsStore
import com.example.landnv4.data.inputs.HomeInputs
import com.example.landnv4.databinding.ActivityPointEditBinding
import com.example.landnv4.ui.ResultItem
import com.example.landnv4.ui.ResultsAdapter
import com.example.landnv4.ui.inputs.RequiredInputsDialog


class HomeActivity : AppCompatActivity(), RequiredInputsDialog.Listener {

    private val PREFS = "prefs"
    private val KEY_NIGHT = "night"
    private lateinit var binding: ActivityHomeBinding
    private lateinit var resultsAdapter: ResultsAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultsAdapter = ResultsAdapter()
        binding.includeResults.rvResults.adapter = resultsAdapter
        binding.includeResults.rvResults.layoutManager = LinearLayoutManager(this)
        binding.includeResults.rvResults.isNestedScrollingEnabled = false
        binding.includeResults.tvResultsTitle.visibility = View.GONE

        // Side button opens inputs dialog
        findViewById<Button>(R.id.btn_inputs).setOnClickListener {
            RequiredInputsDialog().show(supportFragmentManager, "required_inputs")
            refreshStatus()
        }

        // 6 feature buttons
        binding.btnAzimuth.setOnClickListener {
            startActivity(Intent(this, DaylightActivity::class.java))
        }
        binding.btnNorthing.setOnClickListener {
            startActivity(Intent(this, NorthingActivity::class.java))
        }
        binding.btnConvert.setOnClickListener {
            startActivity(Intent(this, ConverterActivity::class.java))
        }
        binding.btnFeature4.setOnClickListener {
            startActivity(Intent(this, NorthingStarMapActivity::class.java)) // change to your activity
        }
        binding.btnDatabank.setOnClickListener {
            startActivity(Intent(this, DataBankActivity::class.java)) // change to your activity
        }
        binding.btnDistance.setOnClickListener {
            startActivity(Intent(this, YeilutActivity::class.java)) // change to your activity

        }


        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }


    private fun refreshStatus() {
        val inputs = AppInputsStore.load(this)
        setFeatureButtonsEnabled((inputs != null))

        val results = if (inputs == null) {
            listOf(ResultItem("N/A", "Enter Required Info to enable features."))
        } else {
            listOf(
                ResultItem("Utm", "E: ${inputs.utm13.easting}, N: ${inputs.utm13.northing}"),
                ResultItem("Date", inputs.dateIso),
                ResultItem("Time", inputs.timeHundredth)
            )
        }

        resultsAdapter.submitList(results)
    }

    /*
     private fun refreshStatus() {
        val inputs = AppInputsStore.load(this)
        setFeatureButtonsEnabled((inputs != null))
        tvStatus.text = if (inputs == null) {
            "Enter Required Inputs to enable features."
        } else {
            "Required inputs set:\nDate: ${inputs.dateIso}\nTime: ${inputs.timeHundredth}\nUTM: ${inputs.utm13}"
        }
    }
     */

    // Called when dialog confirms & saves
    override fun onInputsSaved(inputs: HomeInputs) {
        AppInputsStore.save(this, inputs.dateIso, inputs.timeHundredth, inputs.utm13 )
        refreshStatus()
    }

    private fun setFeatureButtonsEnabled(enabled: Boolean) {
        val buttons = listOf(
            binding.btnAzimuth,
            binding.btnNorthing,
            binding.btnConvert,
            binding.btnFeature4,
            binding.btnDatabank,
            binding.btnDistance
        )

        buttons.forEach { b ->
            b.isEnabled = enabled
            b.alpha = if (enabled) 1.0f else 0.45f
        }
    }


}
