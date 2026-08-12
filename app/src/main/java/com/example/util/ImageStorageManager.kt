package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageManager {

    private const val TAG = "ImageStorageManager"
    private const val MAX_DIMENSION = 1024 // max width/height in pixels
    private const val JPEG_QUALITY = 80     // 80% JPEG quality for high clarity & small size

    /**
     * Reads image from content Uri, resizes it to max 1024px, compresses as JPEG (80% quality),
     * and saves it to local app internal storage (filesDir/bot_images/).
     * Returns local file path / URI.
     */
    suspend fun compressAndSaveImage(context: Context, inputUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // 1. Calculate image dimensions without decoding full bitmap into memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(inputUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight

            if (originalWidth <= 0 || originalHeight <= 0) {
                return@withContext inputUri.toString()
            }

            // 2. Calculate sample size for memory-friendly decoding
            var sampleSize = 1
            while (originalWidth / sampleSize > MAX_DIMENSION || originalHeight / sampleSize > MAX_DIMENSION) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            // 3. Decode sampled bitmap
            val bitmap: Bitmap? = contentResolver.openInputStream(inputUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            }

            if (bitmap == null) {
                return@withContext inputUri.toString()
            }

            // 4. Fine-scale bitmap to exact target bounding size if still larger than MAX_DIMENSION
            val scaledBitmap = if (bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION) {
                val ratio = minOf(
                    MAX_DIMENSION.toFloat() / bitmap.width,
                    MAX_DIMENSION.toFloat() / bitmap.height
                )
                val targetWidth = (bitmap.width * ratio).toInt()
                val targetHeight = (bitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            } else {
                bitmap
            }

            // 5. Create local storage directory
            val imagesDir = File(context.filesDir, "bot_images").apply {
                if (!exists()) mkdirs()
            }

            val filename = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val outputFile = File(imagesDir, filename)

            // 6. Compress and save to JPEG
            FileOutputStream(outputFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            val savedUriString = Uri.fromFile(outputFile).toString()
            Log.d(TAG, "Compressed image successfully saved. Path: $savedUriString, Size: ${outputFile.length() / 1024} KB")
            savedUriString
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compress and save image", e)
            inputUri.toString()
        }
    }

    /**
     * Infrastructure Helper: Prepares image for remote Cloud Storage (Firebase Storage / Cloudflare R2 / S3 REST API).
     * Takes local compressed file path and returns remote cloud URL.
     */
    suspend fun uploadToCloudStorage(
        localUriOrPath: String,
        cloudEndpointUrl: String? = null
    ): String = withContext(Dispatchers.IO) {
        // If it's already an http/https URL, return directly
        if (localUriOrPath.startsWith("http://") || localUriOrPath.startsWith("https://")) {
            return@withContext localUriOrPath
        }

        // Architecture pipeline: If a cloud storage endpoint is provided, upload the file payload
        // e.g. using Retrofit, Ktor, or Firebase Storage SDK:
        // val file = File(Uri.parse(localUriOrPath).path ?: "")
        // val cloudUrl = firebaseStorage.reference.child("avatars/${file.name}").putFile(...)
        
        // Return local file Uri for now when offline
        localUriOrPath
    }
}
