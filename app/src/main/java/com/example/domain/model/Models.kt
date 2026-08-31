package com.example.domain.model

import java.util.UUID

enum class DocumentType(val displayName: String) {
    RECEIPT("Receipt"),
    WHITEBOARD("Whiteboard"),
    BUSINESS_CARD("Business Card"),
    INVOICE("Invoice"),
    NOTES("Notes")
}

data class ActionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dateOrTime: String = "Today",
    val details: String = "",
    val category: String = "General",
    var isChecked: Boolean = false
)

data class ScanRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: String,
    val timestamp: String,
    val isOfflineProcessed: Boolean = true,
    val engineName: String = "Google ML Kit (On-Device OCR)",
    val latencyMs: Long = 35L,
    val rawExtractedText: String = "",
    val items: List<ActionItem> = emptyList(),
    val imageUriString: String? = null
)

data class ExtractionResult(
    val items: List<ActionItem>,
    val rawText: String,
    val engineName: String,
    val latencyMs: Long
)

data class OfficeKitState(
    val isConnected: Boolean = true,
    val deviceName: String = "Paired Workstation (PC)",
    val deviceIp: String = "192.168.1.104",
    val latencyMs: Int = 12,
    val isScreenMirroring: Boolean = false,
    val clipboardSyncEnabled: Boolean = true
)

data class TelemetryState(
    val totalScans: Int = 0,
    val onDeviceInferences: Int = 0,
    val totalInferenceMs: Long = 0L,
    val clipboardSyncs: Int = 0,
    val pcInteractions: Int = 0,
    val pdfExports: Int = 0
) {
    val averageLatencyMs: Int
        get() = if (totalScans > 0) (totalInferenceMs / totalScans).toInt() else 0
}

enum class NavTab {
    HOME,
    TASKS,
    PC_SETTINGS
}

enum class ScreenState {
    MAIN_TABS,
    CAMERA_VIEW,
    PROCESSING_VIEW,
    RESULT_DETAILS
}
