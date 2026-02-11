package com.example.landnv4

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landnv4.databinding.ActivityHomeBinding
import com.example.landnv4.data.inputs.AppInputsStore
import com.example.landnv4.data.inputs.HomeInputs
import com.example.landnv4.databinding.ActivityPointEditBinding
import com.example.landnv4.ui.ResultItem
import com.example.landnv4.ui.ResultsAdapter
import com.example.landnv4.ui.inputs.RequiredInputsDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class HomeActivity : BaseActivity(), RequiredInputsDialog.Listener {

    private val PREFS = "prefs"
    private val KEY_NIGHT = "night"
    private lateinit var binding: ActivityHomeBinding
    private lateinit var resultsAdapter: ResultsAdapter
    override fun getLayoutResId() = R.layout.activity_home

    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge(SystemBarStyle.dark(1))
        val root = contentContainer.getChildAt(0)

        binding = ActivityHomeBinding.bind(root)
        setupToolbar("Home", showBack = false)

        //binding = ActivityHomeBinding.inflate(layoutInflater)
        //setContentView(binding.root)



        resultsAdapter = ResultsAdapter({ openDialog() })
        binding.includeResults.rvResults.adapter = resultsAdapter
        binding.includeResults.rvResults.layoutManager = LinearLayoutManager(this)
        binding.includeResults.rvResults.isNestedScrollingEnabled = false
        binding.includeResults.tvResultsTitle.text = "User Information"


        // Side button opens inputs dialog
        binding.btnSetInputs.setOnClickListener { openDialog() }

        binding.includeResults.resultsRoot.setOnClickListener { openDialog() }
        binding.includeResults.btnMore.visibility = View.VISIBLE
        binding.includeResults.btnMore.setOnClickListener { v ->
            PopupMenu(v.context, v).apply {
                menu.add("Delete")
                menu.add("Edit")
                setOnMenuItemClickListener { item ->
                    if (item.title == "Delete") {
                        MaterialAlertDialogBuilder(this@HomeActivity)
                            .setTitle("Delete?")
                            .setMessage("This will remove the saved user information.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete") { _, _ ->
                                run {
                                    AppInputsStore.clear(this@HomeActivity)
                                    refreshStatus()
                                    openDialog()
                                }
                            }
                            .show()

                        true
                    } else if (item.title == "Edit") {
                        openDialog()
                        true
                    } else false
                }
                show()
            }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        // Hide everything except theme
        menu.findItem(R.id.action_required_inputs)?.isVisible = false
        menu.findItem(R.id.action_home)?.isVisible = false

        return true
    }


    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun openDialog() {
        RequiredInputsDialog().show(supportFragmentManager, "required_inputs")
        refreshStatus()
    }

    private fun refreshStatus() {
        val inputs = AppInputsStore.load(this)
        val hasInputs = inputs != null

        setFeatureButtonsEnabled(hasInputs)

        binding.requiredHeader.visibility = if (hasInputs) View.GONE else View.VISIBLE
        binding.includeResults.root.visibility = if (hasInputs) View.VISIBLE else View.GONE

        if (!hasInputs) {
            resultsAdapter.submitList(emptyList())
            return
        }

        val results = listOf(
            ResultItem("Utm", "E: ${inputs.utm13.easting}, N: ${inputs.utm13.northing}"),
            ResultItem("Date", inputs.dateIso),
            ResultItem("Time", inputs.timeHundredth)
        )
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
