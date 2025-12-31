package com.example.landnv4

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView

class HomeActivity : AppCompatActivity() {

    private val vm: HomeInputsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnDaylight = findViewById<Button>(R.id.btn_daylight)
        val btnNorthing = findViewById<Button>(R.id.btn_northing)
        val btnAzimuth = findViewById<Button>(R.id.btn_northingstarmap)
        val btnConverter = findViewById<Button>(R.id.btn_converter)

        // Block feature buttons until inputs are provided
        btnDaylight.isEnabled = false
        btnNorthing.isEnabled = false
        btnAzimuth.isEnabled = false
        btnConverter.isEnabled = false

        val overlay = findViewById<ComposeView>(R.id.requiredInputsCompose)

        overlay.setContent {
            RequiredInputsDialog { d, t, u ->
                vm.setInputs(d, t, u)

                AppInputsStore.save(this@HomeActivity, d, t, u)

                // Enable buttons now that inputs are valid
                btnDaylight.isEnabled = true
                btnNorthing.isEnabled = true
                btnAzimuth.isEnabled = true
                btnConverter.isEnabled = true

                // Hide overlay completely
                overlay.visibility = View.GONE
            }
        }

        btnDaylight.setOnClickListener {
            startActivity(Intent(this, DaylightActivity::class.java))
        }

        btnConverter.setOnClickListener {
            startActivity(Intent(this, ConverterActivity::class.java))
        }

        btnNorthing.setOnClickListener {
            startActivity(Intent(this, NorthingActivity::class.java))
        }

        btnAzimuth.setOnClickListener {
            startActivity(Intent(this, NorthingStarMapActivity::class.java))
        }
    }
}
