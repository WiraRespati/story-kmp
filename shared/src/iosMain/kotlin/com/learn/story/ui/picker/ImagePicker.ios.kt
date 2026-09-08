package com.learn.story.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSize
import platform.Foundation.NSData
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIWindow
import platform.darwin.NSObject

private const val MAX_IMAGE_SIZE_BYTES = 950_000 // strictly < 1MB
private const val INITIAL_MAX_DIMENSION = 1280.0

@OptIn(ExperimentalForeignApi::class)
private fun compressUIImage(
    image: UIImage,
    maxDimension: Double = INITIAL_MAX_DIMENSION,
    maxBytes: Int = MAX_IMAGE_SIZE_BYTES
): ByteArray? {
    var currentImage = image
    val width = currentImage.size.useContents { this.width }
    val height = currentImage.size.useContents { this.height }
    val maxSide = maxOf(width, height)

    // 1. Initial downscale if max dimension exceeds maxDimension
    if (maxSide > maxDimension) {
        val scale = maxDimension / maxSide
        val newWidth = width * scale
        val newHeight = height * scale
        val newSize = cValue<CGSize> {
            this.width = newWidth
            this.height = newHeight
        }
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        currentImage.drawInRect(cValue<CGRect> {
            origin.x = 0.0
            origin.y = 0.0
            size.width = newWidth
            size.height = newHeight
        })
        val resized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        if (resized != null) {
            currentImage = resized
        }
    }

    // 2. Iteratively reduce compression quality until size <= maxBytes
    var quality = 0.85
    var jpegData: NSData? = UIImageJPEGRepresentation(currentImage, quality)

    while (jpegData != null && jpegData.length.toInt() > maxBytes && quality > 0.1) {
        quality -= 0.1
        jpegData = UIImageJPEGRepresentation(currentImage, quality)
    }

    // 3. If still exceeding maxBytes, iteratively downscale resolution
    while (jpegData != null && jpegData.length.toInt() > maxBytes) {
        val curW = currentImage.size.useContents { this.width } * 0.8
        val curH = currentImage.size.useContents { this.height } * 0.8
        if (curW < 250.0 || curH < 250.0) break

        val smallerSize = cValue<CGSize> {
            this.width = curW
            this.height = curH
        }
        UIGraphicsBeginImageContextWithOptions(smallerSize, false, 1.0)
        currentImage.drawInRect(cValue<CGRect> {
            origin.x = 0.0
            origin.y = 0.0
            size.width = curW
            size.height = curH
        })
        val smallerImg = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        if (smallerImg != null) {
            currentImage = smallerImg
            jpegData = UIImageJPEGRepresentation(currentImage, 0.7)
        } else {
            break
        }
    }

    return jpegData?.let { it.bytes?.readBytes(it.length.toInt()) }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): () -> Unit {
    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun picker(
                picker: PHPickerViewController,
                didFinishPicking: List<*>
            ) {
                picker.dismissViewControllerAnimated(true, null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult
                val provider: NSItemProvider? = result?.itemProvider
                if (provider != null) {
                    provider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                        val image = data?.let { UIImage(data = it) }
                        val bytes = image?.let { compressUIImage(it) }
                        onImagePicked(bytes)
                    }
                } else {
                    onImagePicked(null)
                }
            }
        }
    }

    return {
        val configuration = PHPickerConfiguration().apply {
            selectionLimit = 1
        }
        val picker = PHPickerViewController(configuration = configuration).apply {
            this.delegate = delegate
        }
        val rootViewController = UIApplication.sharedApplication.windows
            .firstOrNull { (it as? UIWindow)?.isKeyWindow() == true }
            ?.let { it as UIWindow }
            ?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCameraLauncher(
    onImageCaptured: (ByteArray?) -> Unit
): () -> Unit {
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                picker.dismissViewControllerAnimated(true, null)
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                if (image != null) {
                    val bytes = compressUIImage(image)
                    onImageCaptured(bytes)
                } else {
                    onImageCaptured(null)
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
                onImageCaptured(null)
            }
        }
    }

    return {
        if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
            val picker = UIImagePickerController().apply {
                this.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                this.delegate = delegate
            }
            val rootViewController = UIApplication.sharedApplication.windows
                .firstOrNull { (it as? UIWindow)?.isKeyWindow() == true }
                ?.let { it as UIWindow }
                ?.rootViewController
            rootViewController?.presentViewController(picker, animated = true, completion = null)
        } else {
            onImageCaptured(null)
        }
    }
}
