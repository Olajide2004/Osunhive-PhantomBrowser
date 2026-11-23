package com.example.data.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object VirtualCameraMediaManager {
  private const val TAG = "VirtualCameraMedia"

  suspend fun processImageUri(context: Context, uri: Uri): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
      val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
      val originalBitmap = BitmapFactory.decodeStream(inputStream)
      inputStream.close()

      if (originalBitmap == null) return@withContext null

      // Scale to max 1280x720 while maintaining aspect ratio
      val maxW = 1280
      val maxH = 720
      val ratio = minOf(maxW.toFloat() / originalBitmap.width, maxH.toFloat() / originalBitmap.height)
      val targetW = if (ratio < 1.0f) (originalBitmap.width * ratio).toInt() else originalBitmap.width
      val targetH = if (ratio < 1.0f) (originalBitmap.height * ratio).toInt() else originalBitmap.height
      val scaledBitmap = if (ratio < 1.0f) {
        Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
      } else {
        originalBitmap
      }

      // Save locally to app private storage
      val filename = "vcam_img_${System.currentTimeMillis()}.jpg"
      val file = File(context.filesDir, filename)
      FileOutputStream(file).use { out ->
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
      }

      // Convert to Base64 data URL
      val byteArrayOutputStream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
      val base64Bytes = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
      val dataUrl = "data:image/jpeg;base64,$base64Bytes"

      Pair(file.absolutePath, dataUrl)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to process virtual camera image", e)
      null
    }
  }

  suspend fun processVideoUri(context: Context, uri: Uri): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
      // 1. Copy video to internal storage
      val filename = "vcam_vid_${System.currentTimeMillis()}.mp4"
      val destFile = File(context.filesDir, filename)
      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output ->
          input.copyTo(output)
        }
      }

      // 2. Extract a poster thumbnail frame
      val retriever = MediaMetadataRetriever()
      var frameBitmap: Bitmap? = null
      try {
        retriever.setDataSource(destFile.absolutePath)
        frameBitmap = retriever.getFrameAtTime(1_000_000) ?: retriever.frameAtTime
      } catch (e: Exception) {
        Log.w(TAG, "Could not extract video frame", e)
      } finally {
        retriever.release()
      }

      val dataUrl = if (frameBitmap != null) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        frameBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val base64Bytes = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64Bytes"
      } else {
        ""
      }

      Pair(destFile.absolutePath, dataUrl)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to process virtual camera video", e)
      null
    }
  }

  suspend fun loadThumbnailBitmap(filePathOrUri: String?): Bitmap? = withContext(Dispatchers.IO) {
    if (filePathOrUri.isNullOrBlank()) return@withContext null
    try {
      val file = File(filePathOrUri)
      if (file.exists()) {
        if (filePathOrUri.endsWith(".mp4", ignoreCase = true)) {
          val retriever = MediaMetadataRetriever()
          try {
            retriever.setDataSource(file.absolutePath)
            return@withContext retriever.getFrameAtTime(1_000_000) ?: retriever.frameAtTime
          } finally {
            retriever.release()
          }
        } else {
          return@withContext BitmapFactory.decodeFile(file.absolutePath)
        }
      }
      null
    } catch (e: Exception) {
      null
    }
  }
}
