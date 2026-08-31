package com.example.domain.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.domain.model.ScanRecord
import java.io.File
import java.io.FileOutputStream

object PdfExportService {

    fun generatePdfReport(context: Context, record: ScanRecord): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 20f
                isFakeBoldText = true
            }
            val subPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 11f
            }
            val accentPaint = Paint().apply {
                color = Color.rgb(220, 100, 20)
                textSize = 13f
                isFakeBoldText = true
            }
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isFakeBoldText = true
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            canvas.drawText("LensFlow AI — Document Report", 40f, 50f, titlePaint)
            canvas.drawText("Document Type: ${record.type} | Scanned: ${record.timestamp}", 40f, 70f, subPaint)
            canvas.drawText("Extraction Engine: ${record.engineName} (${record.latencyMs}ms on-device)", 40f, 86f, subPaint)

            canvas.drawLine(40f, 102f, 555f, 102f, linePaint)
            canvas.drawText("EXTRACTED ACTION ITEMS (${record.items.size})", 40f, 125f, accentPaint)

            var currentY = 150f
            record.items.forEachIndexed { idx, item ->
                val statusPrefix = if (item.isChecked) "[COMPLETED]" else "[PENDING]"
                canvas.drawText("${idx + 1}. $statusPrefix [${item.category}] ${item.title}", 40f, currentY, textPaint)
                canvas.drawText("    Due: ${item.dateOrTime} • ${item.details}", 40f, currentY + 15f, subPaint)
                currentY += 38f
            }

            if (record.rawExtractedText.isNotBlank()) {
                canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, linePaint)
                canvas.drawText("RAW OCR TEXT EXTRACT", 40f, currentY + 25f, accentPaint)
                currentY += 45f
                record.rawExtractedText.lines().take(12).forEach { line ->
                    if (currentY < 800f) {
                        canvas.drawText(line.take(75), 40f, currentY, subPaint)
                        currentY += 15f
                    }
                }
            }

            pdfDocument.finishPage(page)
            val pdfFile = File(context.cacheDir, "LensFlow_${record.type}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()

            FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
