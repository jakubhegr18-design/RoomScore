package com.roomanalyzer.roomscore.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.roomanalyzer.roomscore.ui.theme.AccentCoral
import com.roomanalyzer.roomscore.ui.theme.AccentTeal
import com.roomanalyzer.roomscore.ui.theme.DarkBackground
import com.roomanalyzer.roomscore.ui.theme.TextWhite
import java.nio.ByteBuffer
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    photoCount: Int,
    maxPhotos: Int,
    capturedPhotos: List<Bitmap>,
    isProcessing: Boolean,
    onPhotoTaken: (Bitmap) -> Unit,
    onFinishCapture: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var controller by remember { mutableStateOf<LifecycleCameraController?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> permissionGranted = granted }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!permissionGranted) {
        CameraPermissionDenied(onClose = onClose)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val camController = LifecycleCameraController(ctx)
                    previewView.controller = camController
                    camController.bindToLifecycle(lifecycleOwner)
                    controller = camController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground.copy(alpha = 0.25f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkBackground.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }

                Text(
                    text = "$photoCount / $maxPhotos photos",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    modifier = Modifier
                        .background(DarkBackground.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                IconButton(
                    onClick = {
                        controller?.cameraSelector = if (controller?.cameraSelector == androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA) {
                            androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                        }
                        controller?.bindToLifecycle(lifecycleOwner)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkBackground.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Cameraswitch, contentDescription = "Switch camera", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isProcessing && !isCapturing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = if (photoCount == 0) "Point at your room" else "Take another angle",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "More photos = better accuracy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.7f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Button(
                        onClick = {
                            val camController = controller ?: return@Button
                            isCapturing = true
                            capturePhoto(camController, ContextCompat.getMainExecutor(context)) { bitmap ->
                                isCapturing = false
                                if (bitmap != null) onPhotoTaken(bitmap)
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Icon(Icons.Default.Lens, contentDescription = "Capture", modifier = Modifier.size(36.dp))
                    }

                    if (photoCount > 0) {
                        Spacer(modifier = Modifier.width(24.dp))
                        Button(
                            onClick = onFinishCapture,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing ${photoCount} photos\u2026",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite
                    )
                }
            }

            if (capturedPhotos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(capturedPhotos) { index, bitmap ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, AccentTeal, RoundedCornerShape(8.dp))
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Photo ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun CameraPermissionDenied(onClose: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentCoral
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "RoomScore needs camera access to scan your room.\nPlease grant permission in settings.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite
            )
            Button(
                onClick = onClose,
                modifier = Modifier.padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
            ) {
                Text("Go Back")
            }
        }
    }
}

private fun capturePhoto(
    controller: LifecycleCameraController,
    executor: Executor,
    onComplete: (Bitmap?) -> Unit
) {
    controller.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            try {
                val bitmap = imageProxyToBitmap(image)
                onComplete(bitmap)
            } finally {
                image.close()
            }
        }
        override fun onError(exception: ImageCaptureException) {
            exception.printStackTrace()
            onComplete(null)
        }
    })
}

private const val MAX_IMAGE_SIZE = 800

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val options = android.graphics.BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    val scale = maxOf(
        options.outWidth / MAX_IMAGE_SIZE,
        options.outHeight / MAX_IMAGE_SIZE,
        1
    )
    val decodeOptions = android.graphics.BitmapFactory.Options().apply {
        inSampleSize = scale
    }
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        ?: return null

    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
