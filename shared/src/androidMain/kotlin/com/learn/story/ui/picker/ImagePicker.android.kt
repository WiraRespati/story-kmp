package com.learn.story.ui.picker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
import java.io.File

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

private fun compressImageBytes(bytes: ByteArray, maxDimension: Int = 1280, quality: Int = 80): ByteArray {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        var sampleSize = 1
        while ((options.outWidth / sampleSize) > maxDimension || (options.outHeight / sampleSize) > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return bytes
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bitmap.recycle()
        stream.toByteArray()
    } catch (e: Exception) {
        bytes
    }
}
