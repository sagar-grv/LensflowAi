package com.example.domain.sample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object SampleDocumentGenerator {

    fun createSampleDocumentUri(context: Context, mode: String): Uri {
        val bitmap = Bitmap.createBitmap(800, 1000, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRect(0f, 0f, 800f, 1000f, Paint().apply { color = Color.WHITE })

        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(24f, 24f, 776f, 976f, borderPaint)

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 34f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 22f
        }
        val boldTextPaint = Paint().apply {
            color = Color.BLACK
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
                canvas.drawText("PROJECT SPRINT ROADMAP", 180f, 100f, titlePaint)
                canvas.drawText("• Milestone 1: Finalize on-device OCR inference pipeline", 60f, 200f, textPaint)
                canvas.drawText("• Milestone 2: Verify PC clipboard synchronization demo", 60f, 270f, textPaint)
                canvas.drawText("• Task: Complete accessibility labels & WCAG contrast checks", 60f, 340f, textPaint)
                canvas.drawText("• Deliverable: Export verified PDF summary report", 60f, 410f, textPaint)
                canvas.drawText("Team Lead: Product Engineering", 60f, 520f, boldTextPaint)
            }
            "Business Card" -> {
                canvas.drawText("INNOVATION LABS", 180f, 120f, titlePaint)
                canvas.drawText("Alex Rivera — VP Engineering", 60f, 220f, boldTextPaint)
                canvas.drawText("Email: alex.rivera@example.com", 60f, 290f, textPaint)
                canvas.drawText("Phone: +1 (555) 438-9901", 60f, 350f, textPaint)
                canvas.drawText("Action: Schedule follow-up sync for API integration", 60f, 450f, boldTextPaint)
            }
            "Invoice" -> {
                canvas.drawText("TAX INVOICE #INV-2026-904", 160f, 100f, titlePaint)
                canvas.drawText("Vendor: Apex Cloud Infrastructure Ltd.", 60f, 180f, boldTextPaint)
                canvas.drawText("Due Date: Next Friday | Terms: Net 15", 60f, 240f, textPaint)
                canvas.drawText("Cloud Compute Infrastructure: $1,240.00", 60f, 320f, textPaint)
                canvas.drawText("Network Egress & Bandwidth: $160.00", 60f, 370f, textPaint)
                canvas.drawText("TOTAL DUE: $1,400.00", 60f, 460f, titlePaint)
                canvas.drawText("Action: Approve invoice voucher before due date", 60f, 560f, boldTextPaint)
            }
            else -> {
                canvas.drawText("HANDWRITTEN MEETING NOTES", 120f, 100f, titlePaint)
                canvas.drawText("1. Test Red Light / Green Light PC sync mode", 60f, 190f, textPaint)
                canvas.drawText("2. Check clipboard sync triggers on laptop immediately", 60f, 260f, textPaint)
                canvas.drawText("3. Export PDF summary to PC via universal share", 60f, 330f, textPaint)
                canvas.drawText("4. Verify complete accessibility and TalkBack support", 60f, 400f, boldTextPaint)
            }
        }

        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val sampleFile = File(cacheDir, "sample_${mode.lowercase()}_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(sampleFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(sampleFile)
    }
}
