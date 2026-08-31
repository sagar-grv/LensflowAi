package com.example.data.repository

import com.example.data.local.Converters
import com.example.data.local.dao.ScanDao
import com.example.data.local.entity.toDomain
import com.example.data.local.entity.toEntity
import com.example.domain.model.ActionItem
import com.example.domain.model.ScanRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ScanRepository {
    fun getAllScans(): Flow<List<ScanRecord>>
    suspend fun getScanById(id: String): ScanRecord?
    suspend fun insertScan(scan: ScanRecord)
    suspend fun updateScan(scan: ScanRecord)
    suspend fun toggleActionItem(scanId: String, itemId: String)
    suspend fun deleteScanById(id: String)
    suspend fun deleteAllScans()
}

class ScanRepositoryImpl(
    private val scanDao: ScanDao,
    private val converters: Converters = Converters()
) : ScanRepository {

    override fun getAllScans(): Flow<List<ScanRecord>> {
        return scanDao.getAllScans().map { entities ->
            entities.map { entity ->
                val items = converters.toActionItemList(entity.itemsJson)
                entity.toDomain(items)
            }
        }
    }

    override suspend fun getScanById(id: String): ScanRecord? {
        val entity = scanDao.getScanById(id) ?: return null
        val items = converters.toActionItemList(entity.itemsJson)
        return entity.toDomain(items)
    }

    override suspend fun insertScan(scan: ScanRecord) {
        val itemsJson = converters.fromActionItemList(scan.items)
        scanDao.insertScan(scan.toEntity(itemsJson))
    }

    override suspend fun updateScan(scan: ScanRecord) {
        val itemsJson = converters.fromActionItemList(scan.items)
        scanDao.updateScan(scan.toEntity(itemsJson))
    }

    override suspend fun toggleActionItem(scanId: String, itemId: String) {
        val entity = scanDao.getScanById(scanId) ?: return
        val currentItems = converters.toActionItemList(entity.itemsJson).toMutableList()
        val index = currentItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = currentItems[index]
            currentItems[index] = item.copy(isChecked = !item.isChecked)
            val updatedJson = converters.fromActionItemList(currentItems)
            scanDao.updateScan(entity.copy(itemsJson = updatedJson))
        }
    }

    override suspend fun deleteScanById(id: String) {
        scanDao.deleteScanById(id)
    }

    override suspend fun deleteAllScans() {
        scanDao.deleteAllScans()
    }
}
