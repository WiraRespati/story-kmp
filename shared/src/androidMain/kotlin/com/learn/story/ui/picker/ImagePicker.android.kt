package com.learn.story.ui.picker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

private const val MAX_IMAGE_SIZE_BYTES = 950_000 // strictly < 1MB (1,000,000 bytes)
private const val INITIAL_MAX_DIMENSION = 1280

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val compressed = rawBytes?.let { compressImageBytes(it) }
            onImagePicked(compressed)
        } else {
            onImagePicked(null)
        }
    }
    return { launcher.launch("image/*") }
}

@Composable
actual fun rememberCameraLauncher(
    onImageCaptured: (ByteArray?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var photoFile by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val file = photoFile
            if (file != null && file.exists()) {
                val rawBytes = file.readBytes()
                val compressed = compressImageBytes(rawBytes)
                onImageCaptured(compressed)
                file.delete()
            } else {
                onImageCaptured(null)
            }
        } else {
            photoFile?.delete()
            onImageCaptured(null)
        }
    }

    return {
        try {
            val file = File.createTempFile("camera_photo_", ".jpg", context.cacheDir)
            photoFile = file
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            launcher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            onImageCaptured(null)
        }
    }
}

fun compressImageBytes(
    bytes: ByteArray,
    maxDimension: Int = INITIAL_MAX_DIMENSION,
    maxBytes: Int = MAX_IMAGE_SIZE_BYTES
): ByteArray {
    return try {
        // Read EXIF orientation to maintain proper photo orientation
        val orientation = try {
            val exif = ExifInterface(ByteArrayInputStream(bytes))
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_UNDEFINED
        }

        // 1. Decode bounds only to calculate initial inSampleSize
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

        var sampleSize = 1
        while ((boundsOptions.outWidth / sampleSize) > maxDimension || 
               (boundsOptions.outHeight / sampleSize) > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return bytes

        // 2. Rotate according to EXIF if taken from camera
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
                bitmap = rotated
            }
        }

        // 3. Proportional resize if max dimension exceeds maxDimension
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide > maxDimension) {
            val scale = maxDimension.toFloat() / maxSide
            val targetW = (bitmap.width * scale).toInt()
            val targetH = (bitmap.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            if (scaled != bitmap) {
                bitmap.recycle()
                bitmap = scaled
            }
        }

        // 4. Iteratively reduce compression quality until size <= maxBytes
        var quality = 85
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        var resultBytes = stream.toByteArray()

        while (resultBytes.size > maxBytes && quality > 15) {
            stream = ByteArrayOutputStream()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            resultBytes = stream.toByteArray()
        }

        // 5. If still exceeding maxBytes, iteratively downscale resolution
        while (resultBytes.size > maxBytes && bitmap.width > 250 && bitmap.height > 250) {
            val targetW = (bitmap.width * 0.8).toInt()
            val targetH = (bitmap.height * 0.8).toInt()
            val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            if (scaled != bitmap) {
                bitmap.recycle()
                bitmap = scaled
            }
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            resultBytes = stream.toByteArray()
        }

        bitmap.recycle()
        resultBytes
    } catch (e: Exception) {
        e.printStackTrace()
        bytes
    }
}
