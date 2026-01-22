package com.example.landnv4.data.db.infobank

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsDao {

    // Anchoring
    @Query("SELECT * FROM anchoring_points ORDER BY name ASC")
    fun getAnchoring(): Flow<List<AnchoringPointEntity>>

    @Insert
    suspend fun insertAnchoring(item: AnchoringPointEntity): Long
    @Insert
    suspend fun insertAllAnchoring(items: List<AnchoringPointEntity>)

    @Update
    suspend fun updateAnchoring(item: AnchoringPointEntity)

    @Delete
    suspend fun deleteAnchoring(item: AnchoringPointEntity)

    // Northing
    @Query("SELECT * FROM northing_points ORDER BY name ASC")
    fun getNorthing(): Flow<List<NorthingPointEntity>>

    @Insert
    suspend fun insertNorthing(item: NorthingPointEntity): Long
    @Insert
    suspend fun insertAllNorthing(items: List<NorthingPointEntity>)

    @Update
    suspend fun updateNorthing(item: NorthingPointEntity)

    @Delete
    suspend fun deleteNorthing(item: NorthingPointEntity)

    // Validating
    @Query("SELECT * FROM validating_points ORDER BY name ASC")
    fun getValidating(): Flow<List<ValidatingPointEntity>>

    @Insert
    suspend fun insertValidating(item: ValidatingPointEntity): Long
    @Insert
    suspend fun insertAllValidating(items: List<ValidatingPointEntity>)

    @Update
    suspend fun updateValidating(item: ValidatingPointEntity)

    @Delete
    suspend fun deleteValidating(item: ValidatingPointEntity)

    // Targets
    @Query("SELECT * FROM targets ORDER BY name ASC")
    fun getTargets(): Flow<List<TargetEntity>>

    @Insert
    suspend fun insertTarget(item: TargetEntity): Long
    @Insert
    suspend fun insertAllTargets(items: List<TargetEntity>)

    @Update
    suspend fun updateTarget(item: TargetEntity)

    @Delete
    suspend fun deleteTarget(item: TargetEntity)
}
