package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ScanRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    @Query("SELECT * FROM scan_records ORDER BY createdAt DESC")
    fun getAllScans(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_records WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: String): ScanRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanRecordEntity)

    @Update
    suspend fun updateScan(scan: ScanRecordEntity)

    @Query("DELETE FROM scan_records WHERE id = :id")
    suspend fun deleteScanById(id: String)

    @Query("DELETE FROM scan_records")
    suspend fun deleteAllScans()
}
