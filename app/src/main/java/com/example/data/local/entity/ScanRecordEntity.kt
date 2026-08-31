package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ActionItem
import com.example.domain.model.ScanRecord

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val timestamp: String,
    val isOfflineProcessed: Boolean,
    val engineName: String,
    val latencyMs: Long,
    val rawExtractedText: String,
    val itemsJson: String,
    val imageUriString: String?,
    val createdAt: Long = System.currentTimeMillis()
)

fun ScanRecordEntity.toDomain(items: List<ActionItem>): ScanRecord {
    return ScanRecord(
        id = id,
        title = title,
        type = type,
        timestamp = timestamp,
        isOfflineProcessed = isOfflineProcessed,
        engineName = engineName,
        latencyMs = latencyMs,
        rawExtractedText = rawExtractedText,
        items = items,
        imageUriString = imageUriString
    )
}

fun ScanRecord.toEntity(itemsJson: String): ScanRecordEntity {
    return ScanRecordEntity(
        id = id,
        title = title,
        type = type,
        timestamp = timestamp,
        isOfflineProcessed = isOfflineProcessed,
        engineName = engineName,
        latencyMs = latencyMs,
        rawExtractedText = rawExtractedText,
        itemsJson = itemsJson,
        imageUriString = imageUriString
    )
}
