package com.example.landnv4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.landnv4.data.repo.DaylightRepository
import kotlinx.coroutines.launch

class DaylightActivity : AppCompatActivity() {

    private lateinit var repo: DaylightRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daylight)

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
}
