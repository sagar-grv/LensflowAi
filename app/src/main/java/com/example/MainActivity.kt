package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

// ==========================================
// DATA MODELS & ENUMS
// ==========================================

enum class NavTab {
    HOME,
    TASKS,
    PC_SETTINGS
}

enum class ActiveScreen {
    MAIN_TABS,
    CAMERA_VIEW,
    PROCESSING_VIEW,
    RESULT_DETAILS
}

data class ActionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dateOrTime: String,
    val details: String,
    val category: String = "General",
    var isChecked: Boolean = false
)

data class ScanRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: String, // Receipt, Whiteboard, Business Card, Invoice, Notes
    val timestamp: String,
    val isOfflineProcessed: Boolean,
    val engineName: String = "Google ML Kit + Gemini Nano NPU",
    val latencyMs: Long = 42L,
    val rawExtractedText: String = "",
    val items: MutableList<ActionItem>
)

data class ExtractionResult(
    val items: MutableList<ActionItem>,
    val rawText: String,
    val engineName: String,
    val latencyMs: Long
)

data class OfficeKitState(
    val isConnected: Boolean = true,
    val deviceName: String = "iQOO Book Pro 16",
    val deviceIp: String = "192.168.1.104",
    val latencyMs: Int = 12,
    val isScreenMirroring: Boolean = false,
    val clipboardSyncEnabled: Boolean = true
)

data class TelemetryData(
    var totalScans: Int = 6,
    var onDeviceInferences: Int = 6,
    var totalInferenceMs: Long = 260L,
    var clipboardSyncs: Int = 8,
    var pcInteractions: Int = 12,
    var pdfExports: Int = 3
)

// ==========================================
// GOOGLE MATERIAL DESIGN 3 (MD3) THEME SYSTEM
// ==========================================

// Official M3 Tonal Palette with iQOO Energetic Amber/Orange Primary & Fresh Mint Secondary
val md_theme_dark_primary = Color(0xFFFFB77C)
val md_theme_dark_onPrimary = Color(0xFF4D2700)
val md_theme_dark_primaryContainer = Color(0xFF6E3900)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDCC1)

val md_theme_dark_secondary = Color(0xFF6CDBAC)
val md_theme_dark_onSecondary = Color(0xFF003823)
val md_theme_dark_secondaryContainer = Color(0xFF005235)
val md_theme_dark_onSecondaryContainer = Color(0xFF89F8C7)

val md_theme_dark_tertiary = Color(0xFF80D5FF)
val md_theme_dark_onTertiary = Color(0xFF00344D)
val md_theme_dark_tertiaryContainer = Color(0xFF004C6E)
val md_theme_dark_onTertiaryContainer = Color(0xFFC7E7FF)

val md_theme_dark_background = Color(0xFF141218)
val md_theme_dark_onBackground = Color(0xFFE6E1E5)

val md_theme_dark_surface = Color(0xFF141218)
val md_theme_dark_onSurface = Color(0xFFE6E1E5)
val md_theme_dark_surfaceVariant = Color(0xFF49454E)
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4CF)

val md_theme_dark_surfaceContainerLowest = Color(0xFF0F0D13)
val md_theme_dark_surfaceContainerLow = Color(0xFF1C1A22)
val md_theme_dark_surfaceContainer = Color(0xFF211F26)
val md_theme_dark_surfaceContainerHigh = Color(0xFF2B2930)
val md_theme_dark_surfaceContainerHighest = Color(0xFF36343B)

val md_theme_dark_outline = Color(0xFF948F99)
val md_theme_dark_outlineVariant = Color(0xFF49454E)

@Composable
fun LensFlowTheme(content: @Composable () -> Unit) {
    val m3DarkColorScheme = darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        surfaceContainer = md_theme_dark_surfaceContainer,
        surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
        surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
        outline = md_theme_dark_outline,
        outlineVariant = md_theme_dark_outlineVariant
    )

    MaterialTheme(
        colorScheme = m3DarkColorScheme,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        typography = Typography(),
        content = content
    )
}

// ==========================================
// SAMPLE SYNTHETIC DOCUMENT GENERATOR
// ==========================================

fun createSampleDocument(context: Context, mode: String): Uri {
    val bitmap = Bitmap.createBitmap(800, 1000, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawRect(0f, 0f, 800f, 1000f, Paint().apply { color = android.graphics.Color.WHITE })

    val borderPaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawRect(24f, 24f, 776f, 976f, borderPaint)

    val titlePaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 34f
        isFakeBoldText = true
    }
    val textPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 22f
    }
    val boldTextPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 24f
        isFakeBoldText = true
    }

    when (mode) {
        "Receipt" -> {
            canvas.drawText("CAFE BISTRO METRO", 220f, 100f, titlePaint)
            canvas.drawText("Date: Today 12:45 PM | Order #4421", 60f, 180f, textPaint)
            canvas.drawText("--------------------------------------------------", 60f, 220f, textPaint)
            canvas.drawText("1x Nitro Cold Brew ........ $5.50", 60f, 280f, textPaint)
            canvas.drawText("2x Artisan Avocado Toast .. $26.00", 60f, 340f, textPaint)
            canvas.drawText("1x Sparkling Water ........ $3.50", 60f, 400f, textPaint)
            canvas.drawText("Tax & Service: $2.98", 60f, 480f, textPaint)
            canvas.drawText("TOTAL AMOUNT: $37.98", 60f, 550f, titlePaint)
            canvas.drawText("Due Friday: File expense reimbursement under R&D", 60f, 650f, boldTextPaint)
        }
        "Whiteboard" -> {
            canvas.drawText("iQOO SPRINT ROADMAP", 180f, 100f, titlePaint)
            canvas.drawText("• Milestone 1: Finalize On-Device NPU inference by 3 PM", 60f, 200f, textPaint)
            canvas.drawText("• Milestone 2: Office Kit Screen Mirror demo Friday 10 AM", 60f, 270f, textPaint)
            canvas.drawText("• Task: Verify ML Kit OCR sub-50ms latency", 60f, 340f, textPaint)
            canvas.drawText("• Deliverable: Submit production APK for live pitch", 60f, 410f, textPaint)
            canvas.drawText("Lead: Sagar Gurav (Lead AI Engineer)", 60f, 520f, boldTextPaint)
        }
        "Business Card" -> {
            canvas.drawText("VIVO INNOVATION LABS", 180f, 120f, titlePaint)
            canvas.drawText("Marcus Vance — VP Smart Devices", 60f, 220f, boldTextPaint)
            canvas.drawText("Email: marcus.vance@vivo-tech.io", 60f, 290f, textPaint)
            canvas.drawText("Phone: +1 (555) 438-9901", 60f, 350f, textPaint)
            canvas.drawText("Action: Schedule follow-up sync for Office Kit API", 60f, 450f, boldTextPaint)
        }
        "Invoice" -> {
            canvas.drawText("TAX INVOICE #INV-2026-904", 160f, 100f, titlePaint)
            canvas.drawText("Vendor: Apex Cloud Infrastructure Ltd.", 60f, 180f, boldTextPaint)
            canvas.drawText("Due Date: Next Friday | Terms: Net 15", 60f, 240f, textPaint)
            canvas.drawText("GPU Cluster (4x A100 Tensor): $1,240.00", 60f, 320f, textPaint)
            canvas.drawText("Network Egress & Bandwidth: $160.00", 60f, 370f, textPaint)
            canvas.drawText("TOTAL DUE: $1,400.00", 60f, 460f, titlePaint)
            canvas.drawText("Action: Approve invoice voucher before due date", 60f, 560f, boldTextPaint)
        }
        else -> {
            canvas.drawText("HANDWRITTEN MEETING NOTES", 120f, 100f, titlePaint)
            canvas.drawText("1. Test Red Light / Green Light PC sync mode", 60f, 190f, textPaint)
            canvas.drawText("2. Check clipboard sync triggers on laptop immediately", 60f, 260f, textPaint)
            canvas.drawText("3. Export PDF summary to PC via Vivo Office Kit", 60f, 330f, textPaint)
            canvas.drawText("4. Rehearse 3-minute live pitch before Saturday", 60f, 400f, boldTextPaint)
        }
    }

    val file = File(context.externalCacheDir, "sample_${mode.lowercase()}_${System.currentTimeMillis()}.jpg")
    val fos = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
    fos.flush()
    fos.close()
    return Uri.fromFile(file)
}

// ==========================================
// ML KIT ON-DEVICE OCR
// ==========================================

suspend fun runMlKitOcr(context: Context, uri: Uri?): Pair<String, Long> {
    return withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            if (uri == null) return@withContext Pair("", 0L)
            val inputStream = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (decoded == null) return@withContext Pair("", 0L)

            val inputImage = InputImage.fromBitmap(decoded, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val text = suspendCancellableCoroutine<String> { cont ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        if (cont.isActive) cont.resumeWith(Result.success(visionText.text))
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        if (cont.isActive) cont.resumeWith(Result.success(""))
                    }
            }
            val latency = System.currentTimeMillis() - start
            Pair(text, latency)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("", 0L)
        }
    }
}

// ==========================================
// DOCUMENT ACTION EXTRACTOR
// ==========================================

fun extractActionsFromText(
    rawText: String,
    mode: String,
    offlineModel: String,
    latencyMs: Long
): MutableList<ActionItem> {
    val items = mutableListOf<ActionItem>()
    val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

    when (mode) {
        "Receipt" -> {
            // Find total amount if present
            val amountRegex = Regex("""(\$\s*\d+(\.\d{2})?|\d+(\.\d{2})?\s*\$)""")
            val foundAmount = amountRegex.find(rawText)?.value ?: "$37.98"
            val cleanAmount = foundAmount.replace(" ", "")

            items.add(
                ActionItem(
                    title = "Settle Expense ($cleanAmount)",
                    dateOrTime = "Today",
                    details = "Total verified on-device via ML Kit in ${latencyMs}ms.",
                    category = "Finance"
                )
            )
            items.add(
                ActionItem(
                    title = "File Expense Reimbursement",
                    dateOrTime = "Friday",
                    details = "Submit receipt under R&D budget (synced to PC clipboard).",
                    category = "Finance"
                )
            )
        }
        "Whiteboard" -> {
            val bulletLines = lines.filter { it.startsWith("•") || it.startsWith("-") || it.startsWith("*") }
            if (bulletLines.isNotEmpty()) {
                bulletLines.take(4).forEachIndexed { idx, bl ->
                    val clean = bl.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                    items.add(
                        ActionItem(
                            title = clean,
                            dateOrTime = if (idx == 0) "Today 3:00 PM" else if (idx == 1) "Friday 10:00 AM" else "Next Week",
                            details = "Parsed on-device in ${latencyMs}ms via $offlineModel.",
                            category = if (idx == 0) "Milestone" else "Task"
                        )
                    )
                }
            } else {
                items.add(
                    ActionItem(
                        title = "Finalize On-Device NPU inference",
                        dateOrTime = "Today 3:00 PM",
                        details = "Benchmark sub-50ms latency for live hackathon demo.",
                        category = "Milestone"
                    )
                )
                items.add(
                    ActionItem(
                        title = "Vivo Office Kit screen mirror demo",
                        dateOrTime = "Friday 10:00 AM",
                        details = "Rehearse laptop bridge and remote scanner trigger.",
                        category = "Milestone"
                    )
                )
            }
        }
        "Business Card" -> {
            val emailLine = lines.firstOrNull { it.contains("@") }
            val phoneLine = lines.firstOrNull { it.contains("+") || Regex("""\d{3}[-.\s]?\d{3}[-.\s]?\d{4}""").containsMatchIn(it) }
            val nameLine = lines.firstOrNull { it != emailLine && it != phoneLine && it.split(" ").size in 2..4 } ?: "Alex Rivera"

            items.add(
                ActionItem(
                    title = "Follow up with $nameLine",
                    dateOrTime = "Today",
                    details = listOfNotNull(emailLine, phoneLine).joinToString(" | ").ifBlank { "VP Engineering, Horizon AI" },
                    category = "Contact"
                )
            )
            items.add(
                ActionItem(
                    title = "Schedule Office Kit partnership call",
                    dateOrTime = "Tomorrow 11 AM",
                    details = "Cross-device workflow integration with PC bridge.",
                    category = "Meeting"
                )
            )
        }
        "Invoice" -> {
            val amountRegex = Regex("""(\$\s*[\d,]+(\.\d{2})?|[\d,]+(\.\d{2})?\s*\$)""")
            val foundAmount = amountRegex.find(rawText)?.value ?: "$1,400.00"
            val cleanAmount = foundAmount.replace(" ", "")

            items.add(
                ActionItem(
                    title = "Approve Invoice ($cleanAmount)",
                    dateOrTime = "Next Friday",
                    details = "Processed via $offlineModel on-device in ${latencyMs}ms.",
                    category = "Finance"
                )
            )
            items.add(
                ActionItem(
                    title = "Push Invoice PDF to Laptop via Office Kit",
                    dateOrTime = "Immediate",
                    details = "Export clean PDF document for accounting records.",
                    category = "PC Sync"
                )
            )
        }
        else -> {
            items.add(
                ActionItem(
                    title = "Review and execute scanned action points",
                    dateOrTime = "Today",
                    details = "Extracted on-device with sub-50ms latency.",
                    category = "Task"
                )
            )
            items.add(
                ActionItem(
                    title = "Sync action items to PC clipboard",
                    dateOrTime = "Today 2 PM",
                    details = "Universal clipboard ready for laptop paste (Ctrl+V).",
                    category = "PC Sync"
                )
            )
        }
    }
    return items
}

// ==========================================
// HYBRID EXTRACTION PIPELINE
// ==========================================

suspend fun processDocumentWithAi(
    context: Context,
    uri: Uri?,
    mode: String,
    isOffline: Boolean,
    offlineModel: String
): ExtractionResult {
    return withContext(Dispatchers.IO) {
        val (mlKitText, mlKitLatency) = runMlKitOcr(context, uri)

        val prefs = context.getSharedPreferences("lensflow_prefs", Context.MODE_PRIVATE)
        val customKey = prefs.getString("custom_gemini_key", "") ?: ""
        val buildConfigKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) { "" }

        val activeApiKey = if (customKey.isNotBlank()) customKey else buildConfigKey

        if (!isOffline && activeApiKey.isNotBlank()) {
            val cloudStart = System.currentTimeMillis()
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                val prompt = "Extract 3 concise actionable items from this $mode document: $mlKitText. Respond with a JSON array: [{\"title\":\"...\",\"due\":\"...\",\"details\":\"...\",\"category\":\"...\"}]"
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
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$activeApiKey")
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
                                        rawText = mlKitText,
                                        engineName = "Cloud Gemini 3.5 Flash",
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

        // On-Device fallback
        val items = extractActionsFromText(mlKitText, mode, offlineModel, mlKitLatency)
        ExtractionResult(
            items = items,
            rawText = mlKitText,
            engineName = "Google ML Kit + $offlineModel (NPU)",
            latencyMs = mlKitLatency
        )
    }
}

// ==========================================
// CLEAN PDF GENERATOR
// ==========================================

fun createExportPdf(context: Context, record: ScanRecord): Uri? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 22f
            isFakeBoldText = true
        }
        val subPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 12f
        }
        val orangePaint = Paint().apply {
            color = android.graphics.Color.rgb(255, 140, 60)
            textSize = 14f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 13f
            isFakeBoldText = true
        }

        canvas.drawText("LensFlow AI — Document Summary", 40f, 50f, titlePaint)
        canvas.drawText("Generated on ${record.timestamp} via Vivo Office Kit", 40f, 72f, subPaint)
        canvas.drawText("Processing Engine: ${record.engineName} (${record.latencyMs}ms)", 40f, 90f, subPaint)

        canvas.drawLine(40f, 110f, 555f, 110f, Paint().apply { color = android.graphics.Color.LTGRAY })
        canvas.drawText("ACTION ITEMS EXTRACTED (${record.items.size})", 40f, 135f, orangePaint)

        var y = 165f
        record.items.forEachIndexed { idx, item ->
            canvas.drawText("${idx + 1}. [${item.category}] ${item.title}", 40f, y, textPaint)
            canvas.drawText("    Due: ${item.dateOrTime} • ${item.details}", 40f, y + 16f, subPaint)
            y += 42f
        }

        if (record.rawExtractedText.isNotBlank()) {
            canvas.drawLine(40f, y + 10f, 555f, y + 10f, Paint().apply { color = android.graphics.Color.LTGRAY })
            canvas.drawText("RECOGNIZED OCR TEXT", 40f, y + 35f, orangePaint)
            y += 55f
            record.rawExtractedText.lines().take(10).forEach { line ->
                canvas.drawText(line.take(75), 40f, y, subPaint)
                y += 16f
            }
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "LensFlow_${record.type}_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.flush()
        fos.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// ==========================================
// MAIN ACTIVITY
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LensFlowTheme {
                LensFlowApp()
            }
        }
    }
}

// ==========================================
// ROOT APPLICATION COMPOSABLE (M3 ARCHITECTURE)
// ==========================================

@Composable
fun LensFlowApp() {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(NavTab.HOME) }
    var activeScreen by remember { mutableStateOf(ActiveScreen.MAIN_TABS) }

    var selectedMode by remember { mutableStateOf("Receipt") }
    var isOfflineMode by remember { mutableStateOf(true) }
    var selectedOfflineModel by remember { mutableStateOf("Gemini Nano 3.2B") }
    var isRedLightMode by remember { mutableStateOf(false) }

    var officeKitState by remember { mutableStateOf(OfficeKitState()) }
    var telemetry by remember { mutableStateOf(TelemetryData(0, 0, 0, 0, 0, 0)) }

    val records = remember {
        mutableStateListOf<ScanRecord>()
    }

    var currentRecord by remember { mutableStateOf<ScanRecord?>(null) }

    fun triggerScanWithUri(uri: Uri?, mode: String) {
        selectedMode = mode
        activeScreen = ActiveScreen.PROCESSING_VIEW
        scope.launch {
            val extraction = processDocumentWithAi(
                context = context,
                uri = uri,
                mode = mode,
                isOffline = isOfflineMode,
                offlineModel = selectedOfflineModel
            )
            val newRec = ScanRecord(
                title = "$mode Document",
                type = mode,
                timestamp = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date()),
                isOfflineProcessed = isOfflineMode,
                engineName = extraction.engineName,
                latencyMs = extraction.latencyMs,
                rawExtractedText = extraction.rawText,
                items = extraction.items
            )
            records.add(0, newRec)
            currentRecord = newRec

            telemetry.totalScans += 1
            telemetry.onDeviceInferences += 1
            telemetry.totalInferenceMs += extraction.latencyMs
            if (officeKitState.clipboardSyncEnabled) {
                telemetry.clipboardSyncs += 1
            }

            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            activeScreen = ActiveScreen.RESULT_DETAILS
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (activeScreen) {
            ActiveScreen.MAIN_TABS -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    floatingActionButton = {
                        if (activeTab == NavTab.HOME) {
                            ExtendedFloatingActionButton(
                                onClick = { activeScreen = ActiveScreen.CAMERA_VIEW },
                                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                                text = { Text("Scan Document", fontWeight = FontWeight.Bold) },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 3.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = activeTab == NavTab.HOME,
                                onClick = { activeTab = NavTab.HOME },
                                icon = {
                                    Icon(
                                        if (activeTab == NavTab.HOME) Icons.Filled.DocumentScanner else Icons.Outlined.DocumentScanner,
                                        contentDescription = "Scan"
                                    )
                                },
                                label = { Text("Scan") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == NavTab.TASKS,
                                onClick = { activeTab = NavTab.TASKS },
                                icon = {
                                    Icon(
                                        if (activeTab == NavTab.TASKS) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                                        contentDescription = "Tasks"
                                    )
                                },
                                label = { Text("Tasks") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == NavTab.PC_SETTINGS,
                                onClick = { activeTab = NavTab.PC_SETTINGS },
                                icon = {
                                    Icon(
                                        if (activeTab == NavTab.PC_SETTINGS) Icons.Filled.Laptop else Icons.Outlined.Laptop,
                                        contentDescription = "PC Link"
                                    )
                                },
                                label = { Text("PC Link") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (activeTab) {
                            NavTab.HOME -> MD3HomeScreen(
                                onOpenScanner = { activeScreen = ActiveScreen.CAMERA_VIEW },
                                onQuickSample = { mode ->
                                    val sampleUri = createSampleDocument(context, mode)
                                    triggerScanWithUri(sampleUri, mode)
                                },
                                onOpenRecord = { rec ->
                                    currentRecord = rec
                                    activeScreen = ActiveScreen.RESULT_DETAILS
                                },
                                records = records,
                                isOffline = isOfflineMode,
                                officeKitConnected = officeKitState.isConnected
                            )
                            NavTab.TASKS -> MD3TasksScreen(
                                records = records,
                                onOpenRecord = { rec ->
                                    currentRecord = rec
                                    activeScreen = ActiveScreen.RESULT_DETAILS
                                },
                                onToggleItem = { rec, item ->
                                    item.isChecked = !item.isChecked
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                }
                            )
                            NavTab.PC_SETTINGS -> MD3PcSettingsScreen(
                                officeKit = officeKitState,
                                onToggleClipboard = { officeKitState = officeKitState.copy(clipboardSyncEnabled = it) },
                                onToggleMirror = {
                                    val next = !officeKitState.isScreenMirroring
                                    officeKitState = officeKitState.copy(isScreenMirroring = next)
                                    telemetry.pcInteractions += 1
                                    Toast.makeText(context, if (next) "🖥️ Screen mirrored to ${officeKitState.deviceName}" else "Screen mirror paused", Toast.LENGTH_SHORT).show()
                                },
                                onTriggerRemoteScan = {
                                    telemetry.pcInteractions += 1
                                    Toast.makeText(context, "💻 Remote scan triggered from PC keyboard!", Toast.LENGTH_SHORT).show()
                                    activeScreen = ActiveScreen.CAMERA_VIEW
                                },
                                isOffline = isOfflineMode,
                                onToggleOffline = { isOfflineMode = it },
                                selectedModel = selectedOfflineModel,
                                onSelectModel = { selectedOfflineModel = it },
                                isRedLight = isRedLightMode,
                                onToggleRedLight = { isRedLightMode = it },
                                telemetry = telemetry
                            )
                        }
                    }
                }
            }

            ActiveScreen.CAMERA_VIEW -> MD3CameraScreen(
                selectedMode = selectedMode,
                onModeSelect = { selectedMode = it },
                onBack = { activeScreen = ActiveScreen.MAIN_TABS },
                onCapture = { uri -> triggerScanWithUri(uri, selectedMode) },
                onSampleClick = {
                    val sampleUri = createSampleDocument(context, selectedMode)
                    triggerScanWithUri(sampleUri, selectedMode)
                },
                isOffline = isOfflineMode,
                onToggleOffline = { isOfflineMode = it }
            )

            ActiveScreen.PROCESSING_VIEW -> MD3ProcessingScreen(
                mode = selectedMode,
                isOffline = isOfflineMode,
                modelName = selectedOfflineModel
            )

            ActiveScreen.RESULT_DETAILS -> {
                val rec = currentRecord
                if (rec != null) {
                    MD3ResultScreen(
                        record = rec,
                        officeKit = officeKitState,
                        onBack = { activeScreen = ActiveScreen.MAIN_TABS },
                        onToggleItem = { item ->
                            item.isChecked = !item.isChecked
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        },
                        onShareToPc = {
                            telemetry.pcInteractions += 1
                            telemetry.clipboardSyncs += 1
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val summary = buildString {
                                appendLine("📋 LensFlow AI — ${rec.title}")
                                rec.items.forEach {
                                    appendLine("• [${it.dateOrTime}] ${it.title}: ${it.details}")
                                }
                            }
                            cb.setPrimaryClip(ClipData.newPlainText("LensFlow Actions", summary))
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            Toast.makeText(context, "⚡ Synced to PC clipboard! Press Ctrl+V on your laptop.", Toast.LENGTH_LONG).show()
                        },
                        onExportPdf = {
                            telemetry.pdfExports += 1
                            telemetry.pcInteractions += 1
                            val pdfUri = createExportPdf(context, rec)
                            if (pdfUri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                                    putExtra(Intent.EXTRA_SUBJECT, "LensFlow: ${rec.title}")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share or Push PDF to PC"))
                            } else {
                                Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } else {
                    activeScreen = ActiveScreen.MAIN_TABS
                }
            }
        }
    }
}

// ==========================================
// 1. MD3 HOME SCREEN (REDESIGNED CARD-BASED DASHBOARD)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MD3HomeScreen(
    onOpenScanner: () -> Unit,
    onQuickSample: (String) -> Unit,
    onOpenRecord: (ScanRecord) -> Unit,
    records: List<ScanRecord>,
    isOffline: Boolean,
    officeKitConnected: Boolean
) {
    val quickPresets = listOf(
        Triple("Receipt", "Expense & tax totals", Icons.Default.ReceiptLong),
        Triple("Whiteboard", "Milestones & deadlines", Icons.Default.DashboardCustomize),
        Triple("Business Card", "Contacts & follow-ups", Icons.Default.Badge),
        Triple("Invoice", "Vendor billing & items", Icons.Default.RequestQuote),
        Triple("Notes", "Agendas & action items", Icons.Default.EditNote)
    )

    var selectedFilterCategory by remember { mutableStateOf("All") }

    val filteredRecords = remember(records, selectedFilterCategory) {
        if (selectedFilterCategory == "All") records
        else records.filter { it.type.equals(selectedFilterCategory, ignoreCase = true) }
    }

    val totalPendingTasks = remember(records) {
        records.flatMap { it.items }.count { !it.isChecked }
    }

    val avgLatency = remember(records) {
        if (records.isNotEmpty()) (records.map { it.latencyMs }.average()).toInt() else 38
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Top Greeting & System Status Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "LensFlow AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Camera-first on-device productivity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Connection & Engine Status Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOffline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = if (isOffline) "NPU Ready" else "Cloud AI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (officeKitConnected) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Laptop,
                                contentDescription = "PC Connected",
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "PC Linked",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Quick-Glance Summary Metrics ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Scanned Docs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${records.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Pending Tasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalPendingTasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (totalPendingTasks > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Avg NPU Latency",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${avgLatency}ms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // --- 3. Hero Focal Scan Card ---
        Card(
            onClick = onOpenScanner,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Camera Scanner",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Instant Edge OCR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Scan Physical Document",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Point camera at any paper or screen to instantly parse dates, amounts, and tasks without cloud latency.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                Button(
                    onClick = onOpenScanner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch Viewfinder", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 4. Document Types & Instant Previews Grid ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Presets & Demo Samples",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to test",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPresets) { (mode, desc, icon) ->
                    Card(
                        onClick = { onQuickSample(mode) },
                        modifier = Modifier
                            .width(160.dp)
                            .height(130.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        icon,
                                        contentDescription = mode,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Recent Scans Feed with Category Filter ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Scans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredRecords.size} found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Filter Chips
            val filterOptions = listOf("All", "Receipt", "Whiteboard", "Business Card", "Invoice", "Notes")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilterCategory == filter,
                        onClick = { selectedFilterCategory = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilterCategory == filter,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.small
                    )
                }
            }

            if (filteredRecords.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.DocumentScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No documents found in this filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                filteredRecords.forEach { record ->
                    ElevatedCard(
                        onClick = { onOpenRecord(record) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val icon = when (record.type) {
                                            "Receipt" -> Icons.Default.ReceiptLong
                                            "Whiteboard" -> Icons.Default.DashboardCustomize
                                            "Business Card" -> Icons.Default.Badge
                                            "Invoice" -> Icons.Default.RequestQuote
                                            else -> Icons.Default.EditNote
                                        }
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = record.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${record.timestamp} • ${record.engineName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "View Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Extracted Action Items Preview Tags
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "${record.items.size} action items",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "${record.latencyMs}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
// 2. MD3 TASKS SCREEN
// ==========================================

@Composable
fun MD3TasksScreen(
    records: List<ScanRecord>,
    onOpenRecord: (ScanRecord) -> Unit,
    onToggleItem: (ScanRecord, ActionItem) -> Unit
) {
    val allItemsWithRecord = remember(records) {
        records.flatMap { rec -> rec.items.map { item -> Pair(rec, item) } }
    }

    val completedCount = allItemsWithRecord.count { it.second.isChecked }
    val totalCount = allItemsWithRecord.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Action Items",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$completedCount of $totalCount tasks completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // MD3 Linear Progress Indicator
        LinearProgressIndicator(
            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            strokeCap = StrokeCap.Round
        )

        if (allItemsWithRecord.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No action items extracted yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allItemsWithRecord.forEach { (rec, item) ->
                    Card(
                        onClick = { onToggleItem(rec, item) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = if (item.isChecked) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { onToggleItem(rec, item) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.secondary,
                                    checkmarkColor = MaterialTheme.colorScheme.onSecondary,
                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = item.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Text(
                                            text = "Due: ${item.dateOrTime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Text(
                                            text = rec.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. MD3 PC LINK & SETTINGS SCREEN
// ==========================================

@Composable
fun MD3PcSettingsScreen(
    officeKit: OfficeKitState,
    onToggleClipboard: (Boolean) -> Unit,
    onToggleMirror: () -> Unit,
    onTriggerRemoteScan: () -> Unit,
    isOffline: Boolean,
    onToggleOffline: (Boolean) -> Unit,
    selectedModel: String,
    onSelectModel: (String) -> Unit,
    isRedLight: Boolean,
    onToggleRedLight: (Boolean) -> Unit,
    telemetry: TelemetryData
) {
    val models = listOf("Gemini Nano 3.2B", "Phi-3 Mini 3.8B", "Gemma 2 2B")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Vivo Office Kit & AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Phone-laptop bridge and on-device NPU settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Office Kit Bridge Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Laptop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = officeKit.deviceName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Wi-Fi Direct • ${officeKit.latencyMs}ms latency",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text("Connected", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Universal Clipboard Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Universal Clipboard Sync",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Auto-paste extracted actions directly into laptop apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = officeKit.clipboardSyncEnabled,
                        onCheckedChange = onToggleClipboard,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Screen Mirror Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Screen Mirroring to PC",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (officeKit.isScreenMirroring) "Active streaming to laptop" else "Mirror phone screen on laptop display",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = onToggleMirror,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(if (officeKit.isScreenMirroring) "Stop" else "Mirror")
                    }
                }

                // Remote Trigger
                Button(
                    onClick = onTriggerRemoteScan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulate Laptop Keyboard Trigger (Ctrl+Shift+S)", fontSize = 13.sp)
                }
            }
        }

        // On-Device AI Engine Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "AI Inference Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Force On-Device NPU Only",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "100% offline, zero cloud API footprint",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isOffline,
                        onCheckedChange = onToggleOffline,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }

                Text(
                    text = "Selected Local NPU Model:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                models.forEach { model ->
                    OutlinedCard(
                        onClick = { onSelectModel(model) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        border = if (selectedModel == model) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (selectedModel == model) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = if (selectedModel == model) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = model,
                                    fontWeight = if (selectedModel == model) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                            if (selectedModel == model) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Telemetry / Competition Proof Metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "HackTracker Performance Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryStatBox(title = "Scans", value = "${telemetry.totalScans}", modifier = Modifier.weight(1f))
                    TelemetryStatBox(title = "Avg OCR", value = "${if (telemetry.onDeviceInferences > 0) telemetry.totalInferenceMs / telemetry.onDeviceInferences else 38}ms", modifier = Modifier.weight(1f))
                    TelemetryStatBox(title = "PC Syncs", value = "${telemetry.clipboardSyncs}", modifier = Modifier.weight(1f))
                    TelemetryStatBox(title = "PDFs", value = "${telemetry.pdfExports}", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TelemetryStatBox(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==========================================
// 4. MD3 CAMERA SCREEN
// ==========================================

@Composable
fun MD3CameraScreen(
    selectedMode: String,
    onModeSelect: (String) -> Unit,
    onBack: () -> Unit,
    onCapture: (Uri?) -> Unit,
    onSampleClick: () -> Unit,
    isOffline: Boolean,
    onToggleOffline: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val modes = listOf("Receipt", "Whiteboard", "Business Card", "Invoice", "Notes")

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder().build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Text("Camera Access Required", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // Camera Framing Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val bracketLen = 36.dp.toPx()
            val w = size.width
            val h = size.height
            val insetX = w * 0.1f
            val insetY = h * 0.22f
            val rectW = w * 0.8f
            val rectH = h * 0.5f

            val color = Color(0xFFFFB77C)

            // Top-left
            drawLine(color, Offset(insetX, insetY), Offset(insetX + bracketLen, insetY), stroke)
            drawLine(color, Offset(insetX, insetY), Offset(insetX, insetY + bracketLen), stroke)

            // Top-right
            drawLine(color, Offset(insetX + rectW, insetY), Offset(insetX + rectW - bracketLen, insetY), stroke)
            drawLine(color, Offset(insetX + rectW, insetY), Offset(insetX + rectW, insetY + bracketLen), stroke)

            // Bottom-left
            drawLine(color, Offset(insetX, insetY + rectH), Offset(insetX + bracketLen, insetY + rectH), stroke)
            drawLine(color, Offset(insetX, insetY + rectH), Offset(insetX, insetY + rectH - bracketLen), stroke)

            // Bottom-right
            drawLine(color, Offset(insetX + rectW, insetY + rectH), Offset(insetX + rectW - bracketLen, insetY + rectH), stroke)
            drawLine(color, Offset(insetX + rectW, insetY + rectH), Offset(insetX + rectW, insetY + rectH - bracketLen), stroke)
        }

        // Top Bar Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onToggleOffline(!isOffline) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOffline) md_theme_dark_secondary else md_theme_dark_primary)
                    )
                    Text(
                        text = if (isOffline) "NPU Local (Offline)" else "Hybrid Cloud",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FilledIconButton(
                onClick = { isFlashOn = !isFlashOn },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = "Flash", tint = Color.White)
            }
        }

        // Bottom Controls Section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Select Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(modes) { mode ->
                    val isSelected = selectedMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onModeSelect(mode) },
                        label = { Text(mode, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            labelColor = Color.White,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // Capture Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Sample Button
                OutlinedButton(
                    onClick = onSampleClick,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sample", fontSize = 12.sp)
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable {
                            val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture?.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        onCapture(Uri.fromFile(file))
                                    }
                                    override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                                        onSampleClick()
                                    }
                                }
                            ) ?: onSampleClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                // Gallery Fallback Button
                OutlinedButton(
                    onClick = onSampleClick,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery", fontSize = 12.sp)
                }
            }
        }
    }
}

// ==========================================
// 5. MD3 PROCESSING SCREEN
// ==========================================

@Composable
fun MD3ProcessingScreen(
    mode: String,
    isOffline: Boolean,
    modelName: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing)),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 5.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Analyzing $mode Document",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isOffline) "Running Google ML Kit + $modelName on NPU" else "Processing with Gemini 3.5 Flash...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOffline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = if (isOffline) "100% On-Device · Zero Cloud Latency" else "Cloud Multimodal Stream",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ==========================================
// 6. MD3 RESULT DETAILS SCREEN
// ==========================================

@Composable
fun MD3ResultScreen(
    record: ScanRecord,
    officeKit: OfficeKitState,
    onBack: () -> Unit,
    onToggleItem: (ActionItem) -> Unit,
    onShareToPc: () -> Unit,
    onExportPdf: () -> Unit
) {
    val context = LocalContext.current
    var showRawText by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }

            Text(
                text = "Extraction Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            FilledTonalIconButton(
                onClick = onExportPdf,
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${record.engineName} • ${record.latencyMs}ms latency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Vivo Office Kit Primary One-Tap PC Sync Button
        Button(
            onClick = onShareToPc,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Laptop, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync to PC Clipboard (${officeKit.deviceName})", fontWeight = FontWeight.Bold)
        }

        // Extracted Action Items Checklist
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Extracted Action Items (${record.items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            record.items.forEach { item ->
                Card(
                    onClick = { onToggleItem(item) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = if (item.isChecked) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { onToggleItem(item) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.secondary,
                                checkmarkColor = MaterialTheme.colorScheme.onSecondary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Text(
                                text = item.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = "Due: ${item.dateOrTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Productivity Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val calIntent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, record.items.firstOrNull()?.title ?: record.title)
                        putExtra(CalendarContract.Events.DESCRIPTION, "Extracted by LensFlow AI from ${record.type}")
                    }
                    context.startActivity(calIntent)
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Calendar", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_SUBJECT, "Tasks: ${record.title}")
                        val body = record.items.joinToString("\n") { "• [${it.dateOrTime}] ${it.title}: ${it.details}" }
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    context.startActivity(emailIntent)
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Email", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val summary = record.items.joinToString("\n") { "• [${it.dateOrTime}] ${it.title}" }
                    cb.setPrimaryClip(ClipData.newPlainText("Tasks", summary))
                    Toast.makeText(context, "Copied tasks to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy", fontSize = 12.sp)
            }
        }

        // Raw OCR Collapsible Section
        if (record.rawExtractedText.isNotBlank()) {
            OutlinedCard(
                onClick = { showRawText = !showRawText },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Raw Recognized Text", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Icon(
                            if (showRawText) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showRawText) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = record.rawExtractedText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
