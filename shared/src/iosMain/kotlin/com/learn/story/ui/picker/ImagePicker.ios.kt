package com.learn.story.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIWindow
import platform.darwin.NSObject

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
                        val jpegData: NSData? = image?.let { UIImageJPEGRepresentation(it, 0.75) }
                        val bytes = jpegData?.bytes?.readBytes(jpegData.length.toInt())
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
                    val jpegData: NSData? = UIImageJPEGRepresentation(image, 0.75)
                    val bytes = jpegData?.bytes?.readBytes(jpegData.length.toInt())
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
