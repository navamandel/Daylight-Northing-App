package com.example.landnv4.data.repo

import android.content.Context
import com.example.landnv4.data.daylight.DaylightFile
import com.example.landnv4.data.daylight.DaylightRow
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class DaylightRepository(private val context: Context) {

    private val gson = Gson()

    // Loads and parses the entire JSON once per call (fine for now; can cache later)
    suspend fun findByUtmAndDate(jsonFileName: String, utm: String, date: String): DaylightRow? =
        withContext(Dispatchers.IO) {
            val reader = InputStreamReader(context.assets.open(jsonFileName))
            val file = gson.fromJson(reader, DaylightFile::class.java)
            file.data.firstOrNull { it.utm == utm && it.date == date }
        }
}
