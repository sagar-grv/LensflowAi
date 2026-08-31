package com.example.domain.ocr

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class MlKitOcrEngine(private val context: Context) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeText(uri: Uri?): Pair<String, Long> = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext Pair("", 0L)
        val startTime = System.currentTimeMillis()

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return@withContext Pair("", 0L)

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val extractedText = suspendCancellableCoroutine<String> { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.success(visionText.text))
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.success(""))
                        }
                    }
            }

            val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            Pair(extractedText, latency)
        } catch (e: Exception) {
            Pair("", 0L)
        }
    }
}
