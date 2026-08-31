package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.LensFlowDatabase
import com.example.data.repository.ScanRepository
import com.example.data.repository.ScanRepositoryImpl
import com.example.domain.model.ActionItem
import com.example.domain.model.DocumentType
import com.example.domain.model.ExtractionResult
import com.example.domain.model.NavTab
import com.example.domain.model.OfficeKitState
import com.example.domain.model.ScanRecord
import com.example.domain.model.ScreenState
import com.example.domain.model.TelemetryState
import com.example.domain.ocr.MlKitOcrEngine
import com.example.domain.parser.SmartEntityParser
import com.example.domain.pdf.PdfExportService
import com.example.domain.sample.SampleDocumentGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed interface LensFlowUiEvent {
    data class ShowToast(val message: String) : LensFlowUiEvent
    data object TriggerHapticFeedback : LensFlowUiEvent
    data class OpenPdfShare(val uri: Uri, val title: String) : LensFlowUiEvent
    data class OpenCalendarIntent(val title: String, val details: String) : LensFlowUiEvent
    data class OpenEmailIntent(val subject: String, val body: String) : LensFlowUiEvent
}

data class LensFlowUiState(
    val scans: List<ScanRecord> = emptyList(),
    val currentScan: ScanRecord? = null,
    val activeTab: NavTab = NavTab.HOME,
    val screenState: ScreenState = ScreenState.MAIN_TABS,
    val selectedCategoryFilter: String = "All",
    val selectedScanMode: String = "Receipt",
    val isOfflineMode: Boolean = true,
    val isRedLightMode: Boolean = false,
    val isProcessing: Boolean = false,
    val officeKit: OfficeKitState = OfficeKitState(),
    val telemetry: TelemetryState = TelemetryState()
)

class LensFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository
    private val ocrEngine: MlKitOcrEngine = MlKitOcrEngine(application.applicationContext)

    init {
        val db = LensFlowDatabase.getDatabase(application)
        repository = ScanRepositoryImpl(db.scanDao())
    }

    private val _activeTab = MutableStateFlow(NavTab.HOME)
    private val _screenState = MutableStateFlow(ScreenState.MAIN_TABS)
    private val _currentScan = MutableStateFlow<ScanRecord?>(null)
    private val _selectedFilter = MutableStateFlow("All")
    private val _selectedScanMode = MutableStateFlow("Receipt")
    private val _isOfflineMode = MutableStateFlow(true)
    private val _isRedLightMode = MutableStateFlow(false)
    private val _isProcessing = MutableStateFlow(false)
    private val _officeKitState = MutableStateFlow(OfficeKitState())
    private val _telemetryState = MutableStateFlow(TelemetryState())

    private val _eventFlow = MutableSharedFlow<LensFlowUiEvent>()
    val eventFlow: SharedFlow<LensFlowUiEvent> = _eventFlow.asSharedFlow()

    val uiState: StateFlow<LensFlowUiState> = combine(
        repository.getAllScans(),
        _activeTab,
        _screenState,
        _currentScan,
        _selectedFilter,
        _selectedScanMode,
        _isOfflineMode,
        _isRedLightMode,
        _isProcessing,
        _officeKitState,
        _telemetryState
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val scans = args[0] as List<ScanRecord>
        val tab = args[1] as NavTab
        val screen = args[2] as ScreenState
        val currentScan = args[3] as? ScanRecord
        val filter = args[4] as String
        val mode = args[5] as String
        val isOffline = args[6] as Boolean
        val isRedLight = args[7] as Boolean
        val isProcessing = args[8] as Boolean
        val officeKit = args[9] as OfficeKitState
        val telemetry = args[10] as TelemetryState

        LensFlowUiState(
            scans = scans,
            currentScan = currentScan ?: scans.firstOrNull(),
            activeTab = tab,
            screenState = screen,
            selectedCategoryFilter = filter,
            selectedScanMode = mode,
            isOfflineMode = isOffline,
            isRedLightMode = isRedLight,
            isProcessing = isProcessing,
            officeKit = officeKit,
            telemetry = telemetry
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LensFlowUiState()
    )

    fun setNavTab(tab: NavTab) {
        _activeTab.value = tab
    }

    fun setScreenState(screen: ScreenState) {
        _screenState.value = screen
    }

    fun setCategoryFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setScanMode(mode: String) {
        _selectedScanMode.value = mode
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
    }

    fun toggleRedLightMode(enabled: Boolean) {
        _isRedLightMode.value = enabled
    }

    fun selectScanRecord(record: ScanRecord) {
        _currentScan.value = record
        _screenState.value = ScreenState.RESULT_DETAILS
    }

    fun toggleActionItem(scanId: String, itemId: String) {
        viewModelScope.launch {
            repository.toggleActionItem(scanId, itemId)
            _eventFlow.emit(LensFlowUiEvent.TriggerHapticFeedback)
        }
    }

    fun deleteScan(id: String) {
        viewModelScope.launch {
            repository.deleteScanById(id)
            if (_currentScan.value?.id == id) {
                _screenState.value = ScreenState.MAIN_TABS
            }
            _eventFlow.emit(LensFlowUiEvent.ShowToast("Document deleted"))
        }
    }

    fun triggerScan(uri: Uri?, mode: String) {
        _selectedScanMode.value = mode
        _isProcessing.value = true
        _screenState.value = ScreenState.PROCESSING_VIEW

        viewModelScope.launch {
            val isOffline = _isOfflineMode.value
            val extraction = processDocumentInternal(uri, mode, isOffline)

            val record = ScanRecord(
                title = "$mode Document",
                type = mode,
                timestamp = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date()),
                isOfflineProcessed = isOffline,
                engineName = extraction.engineName,
                latencyMs = extraction.latencyMs,
                rawExtractedText = extraction.rawText,
                items = extraction.items,
                imageUriString = uri?.toString()
            )

            repository.insertScan(record)
            _currentScan.value = record
            _isProcessing.value = false

            val currentTelemetry = _telemetryState.value
            _telemetryState.value = currentTelemetry.copy(
                totalScans = currentTelemetry.totalScans + 1,
                onDeviceInferences = if (isOffline) currentTelemetry.onDeviceInferences + 1 else currentTelemetry.onDeviceInferences,
                totalInferenceMs = currentTelemetry.totalInferenceMs + extraction.latencyMs,
                clipboardSyncs = if (_officeKitState.value.clipboardSyncEnabled) currentTelemetry.clipboardSyncs + 1 else currentTelemetry.clipboardSyncs
            )

            _eventFlow.emit(LensFlowUiEvent.TriggerHapticFeedback)
            _screenState.value = ScreenState.RESULT_DETAILS
        }
    }

    fun triggerSampleScan(mode: String) {
        val sampleUri = SampleDocumentGenerator.createSampleDocumentUri(getApplication(), mode)
        triggerScan(sampleUri, mode)
    }

    fun copyToClipboard(record: ScanRecord) {
        val context = getApplication<Application>().applicationContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val summary = buildString {
            appendLine("📋 LensFlow AI — ${record.title}")
            record.items.forEach {
                val check = if (it.isChecked) "[✓]" else "[ ]"
                appendLine("$check [${it.dateOrTime}] ${it.title}: ${it.details}")
            }
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("LensFlow Actions", summary))

        val currentTelemetry = _telemetryState.value
        _telemetryState.value = currentTelemetry.copy(
            clipboardSyncs = currentTelemetry.clipboardSyncs + 1,
            pcInteractions = currentTelemetry.pcInteractions + 1
        )

        viewModelScope.launch {
            _eventFlow.emit(LensFlowUiEvent.TriggerHapticFeedback)
            _eventFlow.emit(LensFlowUiEvent.ShowToast("Copied to PC & phone clipboard! Ready for Ctrl+V."))
        }
    }

    fun exportPdf(record: ScanRecord) {
        val pdfUri = PdfExportService.generatePdfReport(getApplication(), record)
        if (pdfUri != null) {
            val currentTelemetry = _telemetryState.value
            _telemetryState.value = currentTelemetry.copy(
                pdfExports = currentTelemetry.pdfExports + 1,
                pcInteractions = currentTelemetry.pcInteractions + 1
            )
            viewModelScope.launch {
                _eventFlow.emit(LensFlowUiEvent.OpenPdfShare(pdfUri, record.title))
            }
        } else {
            viewModelScope.launch {
                _eventFlow.emit(LensFlowUiEvent.ShowToast("Failed to generate PDF report"))
            }
        }
    }

    fun createCalendarEvent(item: ActionItem) {
        viewModelScope.launch {
            _eventFlow.emit(LensFlowUiEvent.OpenCalendarIntent(item.title, item.details))
        }
    }

    fun sendEmailSummary(item: ActionItem) {
        viewModelScope.launch {
            _eventFlow.emit(LensFlowUiEvent.OpenEmailIntent(item.title, item.details))
        }
    }

    fun toggleScreenMirror() {
        val current = _officeKitState.value
        val next = !current.isScreenMirroring
        _officeKitState.value = current.copy(isScreenMirroring = next)
        val currentTelemetry = _telemetryState.value
        _telemetryState.value = currentTelemetry.copy(pcInteractions = currentTelemetry.pcInteractions + 1)
        viewModelScope.launch {
            _eventFlow.emit(LensFlowUiEvent.ShowToast(if (next) "Screen mirrored to ${current.deviceName}" else "Screen mirror paused"))
        }
    }

    fun toggleClipboardSync(enabled: Boolean) {
        _officeKitState.value = _officeKitState.value.copy(clipboardSyncEnabled = enabled)
    }

    private suspend fun processDocumentInternal(
        uri: Uri?,
        mode: String,
        isOffline: Boolean
    ): ExtractionResult = withContext(Dispatchers.IO) {
        val (ocrText, ocrLatency) = ocrEngine.recognizeText(uri)

        val prefs = getApplication<Application>().getSharedPreferences("lensflow_prefs", Context.MODE_PRIVATE)
        val customKey = prefs.getString("custom_gemini_key", "") ?: ""
        val buildConfigKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) { "" }

        val activeKey = if (customKey.isNotBlank()) customKey else buildConfigKey

        if (!isOffline && activeKey.isNotBlank()) {
            val cloudStart = System.currentTimeMillis()
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val prompt = "Extract 3 concise actionable items from this $mode document text: \"$ocrText\". Respond with ONLY a valid JSON array: [{\"title\":\"...\",\"due\":\"...\",\"details\":\"...\",\"category\":\"...\"}]"
                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$activeKey")
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val respObj = JSONObject(body)
                            val candidates = respObj.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val content = candidates.getJSONObject(0).optJSONObject("content")
                                val text = content?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""
                                val cleaned = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                                val cloudItems = mutableListOf<ActionItem>()
                                val arr = JSONArray(cleaned)
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    cloudItems.add(
                                        ActionItem(
                                            title = obj.optString("title", "Action ${i + 1}"),
                                            dateOrTime = obj.optString("due", "Today"),
                                            details = obj.optString("details", ""),
                                            category = obj.optString("category", "General")
                                        )
                                    )
                                }
                                if (cloudItems.isNotEmpty()) {
                                    return@withContext ExtractionResult(
                                        items = cloudItems,
                                        rawText = ocrText,
                                        engineName = "Google Gemini 2.5 Flash (Cloud)",
                                        latencyMs = System.currentTimeMillis() - cloudStart
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fast Local ML Kit & Smart Entity Parser
        val items = SmartEntityParser.parseActions(ocrText, mode, ocrLatency)
        ExtractionResult(
            items = items,
            rawText = ocrText,
            engineName = "Google ML Kit (On-Device OCR)",
            latencyMs = ocrLatency
        )
    }
}
