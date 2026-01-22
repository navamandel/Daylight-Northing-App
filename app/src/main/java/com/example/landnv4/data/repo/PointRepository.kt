package com.example.landnv4.data.repo

import android.content.Context
import com.example.landnv4.data.db.AppDatabase
import com.example.landnv4.data.db.infobank.AnchoringPointEntity
import com.example.landnv4.data.db.infobank.NorthingPointEntity
import com.example.landnv4.data.db.infobank.TargetEntity
import com.example.landnv4.data.db.infobank.ValidatingPointEntity
import com.example.landnv4.ui.databank.PointItem
import com.example.landnv4.ui.databank.PointItems
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class PointRepository() {
    private val gson = Gson()



    suspend fun loadPointsFromFile(context: Context) {
        val reader = context.loadJsonFromAssets("sample_points.json")
        val data = gson.fromJson(reader, PointItems::class.java)
        val db = AppDatabase.getInstance(context)

        db.pointsDao().insertAllAnchoring(
            data.anchoring_points.map {
                AnchoringPointEntity(
                    utm = it.utm,
                    location = it.location,
                    height = it.height,
                    heightType = it.heightType,
                    name = it.name
                )
            }
        )

        db.pointsDao().insertAllNorthing(
            data.northing_points.map {
                NorthingPointEntity(
                    utm = it.utm,
                    location = it.location,
                    height = it.height,
                    heightType = it.heightType,
                    name = it.name
                )
            }
        )

        db.pointsDao().insertAllValidating(
            data.validating_points.map {
                ValidatingPointEntity(
                    utm = it.utm,
                    location = it.location,
                    height = it.height,
                    heightType = it.heightType,
                    name = it.name
                )
            }
        )

        db.pointsDao().insertAllTargets(
            data.targets.map {
                TargetEntity(
                    utm = it.utm,
                    location = it.location,
                    height = it.height,
                    heightType = it.heightType,
                    name = it.name
                )
            }
        )
    }

    fun Context.loadJsonFromAssets(fileName: String): String {
        return assets.open(fileName).bufferedReader().use { it.readText() }
    }

}

/*
// Loads and parses the entire JSON once per call (fine for now; can cache later)
    suspend fun findByUtmAndDate(jsonFileName: String, utm: String, date: String): DaylightRow? =
        withContext(Dispatchers.IO) {
            val reader = InputStreamReader(context.assets.open(jsonFileName))
            val file = gson.fromJson(reader, DaylightFile::class.java)
            file.data.firstOrNull { it.utm == utm && it.date == date }
        }

   un loadHygStars(context: Context, fileName: String, maxRows: Int = 5000): List<StarEntity> {
        val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
        val headerLine = reader.readLine() ?: return emptyList()
        val headers = splitCsvLine(headerLine).map { it.trim().trim('"') }

        fun idx(name: String): Int {
            val i = headers.indexOf(name)
            if (i < 0) error("CSV header missing required column: $name")
            return i
        }

        val idIdx = idx("id")
        val hipIdx = idx("hip")
        val properIdx = idx("proper")
        val raIdx = idx("ra")
        val decIdx = idx("dec")
        val magIdx = idx("mag")

        val out = ArrayList<StarEntity>(maxRows)

        while (true) {
            val line = reader.readLine() ?: break
            if (line.isBlank()) continue

            val cols = splitCsvLine(line)
            if (cols.size <= decIdx) continue

            val id = cols[idIdx].toIntOrNull() ?: continue
            val hip = cols.getOrNull(hipIdx)?.toIntOrNull()
            val proper = cols.getOrNull(properIdx)?.trim()?.ifBlank { null }
            val ra = cols[raIdx].toDoubleOrNull() ?: continue
            val dec = cols[decIdx].toDoubleOrNull() ?: continue
            val mag = cols.getOrNull(magIdx)?.toDoubleOrNull()

            out.add(
                StarEntity(
                    id = id,
                    hip = hip,
                    proper = proper,
                    ra = ra,
                    dec = dec,
                    mag = mag
                )
            )

            // if (out.size >= maxRows) break

        }

        return out
 */