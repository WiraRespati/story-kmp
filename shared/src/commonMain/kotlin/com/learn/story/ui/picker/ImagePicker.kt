package com.learn.story.ui.picker

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): () -> Unit

@Composable
expect fun rememberCameraLauncher(
    onImageCaptured: (ByteArray?) -> Unit
): () -> Unit
