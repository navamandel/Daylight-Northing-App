package com.example.landnv4.ui.databank

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.landnv4.data.db.infobank.AnchoringPointEntity
import com.example.landnv4.data.db.infobank.NorthingPointEntity
import com.example.landnv4.data.db.infobank.TargetEntity
import com.example.landnv4.data.db.infobank.ValidatingPointEntity
import com.example.landnv4.databank.BankType
import androidx.lifecycle.viewModelScope
import com.example.landnv4.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PointsListViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).pointsDao()

    fun pointsFlow(type: BankType): Flow<List<PointItem>> {
        return when (type) {
            BankType.ANCHORING -> dao.getAnchoring().map { list -> list.map { it.toItem() } }
            BankType.NORTHING -> dao.getNorthing().map { list -> list.map { it.toItem() } }
            BankType.VALIDATING -> dao.getValidating().map { list -> list.map { it.toItem() } }
            BankType.TARGETS -> dao.getTargets().map { list -> list.map { it.toItem() } }
        }
    }

    suspend fun getPointsOnce(type: BankType): List<PointItem> {
        // If repo returns Flow<List<PointItem>>
        return pointsFlow(type).first()
    }

    fun insert(type: BankType, item: PointItem) {
        viewModelScope.launch {
            when (type) {
                BankType.ANCHORING -> dao.insertAnchoring(item.toAnchoringEntity(id = 0))
                BankType.NORTHING -> dao.insertNorthing(item.toNorthingEntity(id = 0))
                BankType.VALIDATING -> dao.insertValidating(item.toValidatingEntity(id = 0))
                BankType.TARGETS -> dao.insertTarget(item.toTargetEntity(id = 0))
            }
        }
    }

    fun update(type: BankType, item: PointItem) {
        viewModelScope.launch {
            when (type) {
                BankType.ANCHORING -> dao.updateAnchoring(item.toAnchoringEntity(item.id))
                BankType.NORTHING -> dao.updateNorthing(item.toNorthingEntity(item.id))
                BankType.VALIDATING -> dao.updateValidating(item.toValidatingEntity(item.id))
                BankType.TARGETS -> dao.updateTarget(item.toTargetEntity(item.id))
            }
        }
    }

    fun delete(type: BankType, item: PointItem) {
        viewModelScope.launch {
            when (type) {
                BankType.ANCHORING -> dao.deleteAnchoring(item.toAnchoringEntity(item.id))
                BankType.NORTHING -> dao.deleteNorthing(item.toNorthingEntity(item.id))
                BankType.VALIDATING -> dao.deleteValidating(item.toValidatingEntity(item.id))
                BankType.TARGETS -> dao.deleteTarget(item.toTargetEntity(item.id))
            }
        }
    }
}

/** Mapping helpers */
private fun AnchoringPointEntity.toItem() = PointItem(id, utm, location, height, heightType, name)
private fun NorthingPointEntity.toItem() = PointItem(id, utm, location, height, heightType, name)
private fun ValidatingPointEntity.toItem() = PointItem(id, utm, location, height, heightType, name)
private fun TargetEntity.toItem() = PointItem(id, utm, location, height, heightType, name)

private fun PointItem.toAnchoringEntity(id: Long) = AnchoringPointEntity(id, utm, location, height, heightType, name)
private fun PointItem.toNorthingEntity(id: Long) = NorthingPointEntity(id, utm, location, height, heightType, name)
private fun PointItem.toValidatingEntity(id: Long) = ValidatingPointEntity(id, utm, location, height, heightType, name)
private fun PointItem.toTargetEntity(id: Long) = TargetEntity(id, utm, location, height, heightType, name)
