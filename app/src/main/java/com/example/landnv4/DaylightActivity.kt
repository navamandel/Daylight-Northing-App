package com.example.landnv4

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.landnv4.data.repo.DaylightRepository
import com.example.landnv4.ui.inputs.RequiredInputsDialog
import kotlinx.coroutines.launch

class DaylightActivity : BaseActivity() {

    private lateinit var repo: DaylightRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daylight)

        setupToolbar("Daylight")

        repo = DaylightRepository(this)

        val utmInput = findViewById<EditText>(R.id.input_utm)
        val dateInput = findViewById<EditText>(R.id.input_date)
        val outputText = findViewById<TextView>(R.id.output_text)

        findViewById<Button>(R.id.btn_calculate).setOnClickListener {
            val utm = utmInput.text.toString()
            val date = dateInput.text.toString()

            // Offline lookup simulation
            lifecycleScope.launch {
                val row = repo.findByUtmAndDate(
                    jsonFileName = "sunrise_sunset_times.json",
                    utm = utm,
                    date = date
                )

                outputText.text = if (row == null) {
                    "No data found for $utm on $date"
                } else {
                    "Sunrise: ${row.sunrise}\nSunset: ${row.sunset}\nAstro twilight: ${row.twilight}"
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            android.R.id.home -> {
                // Back button (up arrow)
                onBackPressedDispatcher.onBackPressed()
                true
            }

            R.id.action_home -> {
                // Go to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                true
            }

            R.id.action_required_inputs -> {
                // Open your dialog/screen
                RequiredInputsDialog().show(supportFragmentManager, "RequiredInputs")
                true
            }

            R.id.action_theme -> {
                val current = AppCompatDelegate.getDefaultNightMode()
                val next = if (current == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES

                AppCompatDelegate.setDefaultNightMode(next)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
