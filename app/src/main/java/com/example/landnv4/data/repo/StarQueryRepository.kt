package com.example.landnv4.data.repo

import com.example.landnv4.App
import com.example.landnv4.domain.model.Star
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StarQueryRepository {

    suspend fun getAllStars(): List<Star> = withContext(Dispatchers.IO) {
        App.db.starDao().getAllStars().map { e ->
            Star(
                id = e.id,
                name = e.proper?.takeIf { it.isNotBlank() } ?: "Star ${e.id}",
                raDeg = e.ra * 15.0,
                decDeg = e.dec,
                mag = e.mag
            )
        }
    }

    suspend fun countStars(): Int = withContext(Dispatchers.IO) {
        // Add this DAO method if you want a fast preload check (recommended)
         App.db.starDao().countStars()

        // App.db.starDao().getAllStars().size // less efficient, but no DAO change needed
    }
}
