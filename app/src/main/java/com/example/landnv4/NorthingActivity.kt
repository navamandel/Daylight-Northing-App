package com.example.landnv4

import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.landnv4.data.db.AppDatabase
import com.example.landnv4.data.db.StarEntity
import com.example.landnv4.data.repo.StarRepository
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class NorthingActivity : AppCompatActivity() {

    private lateinit var etUtm: EditText
    private lateinit var etDateTime: EditText
    private lateinit var rbSun: RadioButton
    private lateinit var rbStars: RadioButton
    private lateinit var btnPlot: Button
    private lateinit var btnReset: Button
    private lateinit var chart: LineChart
    private lateinit var tvStatus: TextView
    private lateinit var repo: StarRepository

    @RequiresApi(Build.VERSION_CODES.O)
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_northing)

        repo = StarRepository(this)

        etUtm = findViewById(R.id.etUtm)
        etDateTime = findViewById(R.id.etDateTime)
        rbSun = findViewById(R.id.rbSun)
        rbStars = findViewById(R.id.rbStars)
        btnPlot = findViewById(R.id.btnPlot)
        btnReset = findViewById(R.id.btnReset)
        chart = findViewById(R.id.lineChart)
        tvStatus = findViewById(R.id.tvStatus)

        lifecycleScope.launch {
            tvStatus.text = "Status: loading star database..."
            repo.ensurePreloadedFromAssets()
            tvStatus.text = "Status: ready"
        }

        setupChart()

        btnPlot.setOnClickListener { onPlotClicked() }
        btnReset.setOnClickListener { resetUi() }
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = true

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        chart.axisLeft.setDrawGridLines(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onPlotClicked() {
        val utm = etUtm.text.toString().trim()
        if (!utm.matches(Regex("^\\d{12}$"))) {
            tvStatus.text = "Status: UTM must be exactly 12 digits"
            return
        }

        val dtText = etDateTime.text.toString().trim()
        val start: LocalDateTime = try {
            LocalDateTime.parse(dtText, fmt)
        } catch (e: DateTimeParseException) {
            tvStatus.text = "Status: Date/time must be YYYY-MM-DD HH:mm"
            return
        }

        tvStatus.text = "Status: plotting…"

        if (rbSun.isChecked) {
            // Offline sun plot (replace the mills calc with your real formula)
            val entries = buildMillsTimeSeries(start) { t ->
                // TODO: replace with real sun angular mills calc (offline math)
                // Example placeholder curve:
                val minutes = t.minute + t.hour * 60
                (10.0 + 5.0 * kotlin.math.sin(minutes / 30.0)).toFloat()
            }

            showSeries("Sun", entries)
            tvStatus.text = "Status: done (Sun)"
        } else {
            // Stars come from Room DB (offline)
            lifecycleScope.launch {
                try {
                    val stars = withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(applicationContext)
                            .starDao()
                            .getAllStars() // implement in DAO
                    }

                    if (stars.isEmpty()) {
                        tvStatus.text = "Status: no stars in DB (did you preload?)"
                        chart.clear()
                        return@launch
                    }

                    // Build an aggregate “mills vs time” curve from star set
                    val entries = buildMillsTimeSeries(start) { t ->
                        // TODO: replace this with your real calculation using:
                        // - UTM (utm)
                        // - datetime (t)
                        // - star positions from DB (stars)
                        // For now, make a stable deterministic value:
                        calcAggregateStarMills(stars, t).toFloat()
                    }

                    showSeries("Stars", entries)
                    tvStatus.text = "Status: done (Stars)"
                } catch (e: Exception) {
                    tvStatus.text = "Status: error - ${e.message}"
                    chart.clear()
                }
            }
        }
    }

    /**
     * Generates points for "mills as a function of time"
     * e.g. 2 hours window, every 5 minutes (dynamic chart)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildMillsTimeSeries(
        start: LocalDateTime,
        millsAt: (LocalDateTime) -> Float
    ): List<Entry> {
        val entries = ArrayList<Entry>()
        val steps = 24 // 24 * 5min = 120 minutes
        for (i in 0..steps) {
            val t = start.plusMinutes((i * 5).toLong())
            val y = millsAt(t)
            entries.add(Entry(i.toFloat(), y))
        }
        return entries
    }

    private fun showSeries(label: String, entries: List<Entry>) {
        val dataSet = LineDataSet(entries, label).apply {
            setDrawCircles(false)
            lineWidth = 2f
            setDrawValues(false)
        }
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun resetUi() {
        etUtm.setText("")
        etDateTime.setText("")
        rbSun.isChecked = true
        chart.clear()
        tvStatus.text = "Status: ready"
    }

    /**
     * Placeholder aggregation. Replace with your real formula.
     * This just produces a smooth-ish changing value based on star magnitudes and time.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcAggregateStarMills(stars: List<StarEntity>, t: LocalDateTime): Double {
        val timeFactor = (t.hour * 60 + t.minute) / 1440.0
        val brightness = stars.take(200).sumOf { s -> 1.0 / (1.0 + (s.mag ?: 6.0)) }
        return 20.0 + 3.0 * kotlin.math.sin(2.0 * Math.PI * timeFactor) + (brightness % 5.0)
    }
}
