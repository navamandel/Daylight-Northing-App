package com.example.landnv4.data.db.stars

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.landnv4.data.db.stars.StarEntity

@Dao
interface StarDao {
    @Query("SELECT * FROM stars WHERE id != 0")
    suspend fun getAllStars(): List<StarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StarEntity>)

    @Query("SELECT COUNT(*) FROM stars")
    suspend fun countStars(): Int

}