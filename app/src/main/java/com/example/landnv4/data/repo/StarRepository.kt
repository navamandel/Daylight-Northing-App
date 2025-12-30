package com.example.landnv4.data.repo

import android.content.Context
import com.example.landnv4.App
import com.example.landnv4.data.loader.CsvStarLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StarRepository(private val context: Context) {

    suspend fun ensurePreloadedFromAssets() = withContext(Dispatchers.IO) {
        val dao = App.db.starDao()
        if (dao.countStars() > 0) return@withContext

        val stars = CsvStarLoader.loadHygStars(
            context = context,
            fileName = "hygdata_v41.csv",
            maxRows = 50000
        )
        dao.insertAll(stars)
    }




}
