package com.example.mobileproject.presentation.ui.screen.wallet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Quet QR that: CameraX + MLKit.
 * Tra ve rawValue cho man truoc (payout/transfer).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onNavigateBack: () -> Unit,
    onManualInput: () -> Unit,
    onScanned: (rawValue: String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colorScheme = MaterialTheme.colorScheme

    var hasCameraPermission by remember { mutableStateOf(hasCameraPermission(context)) }
    var isScanningEnabled by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted },
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scanQrFromImageUri(
                    context = context,
                    uri = uri,
                    onFound = { raw ->
                        isScanningEnabled = false
                        onScanned(raw)
                    },
                    onNotFound = { errorMessage = "Khong tim thay ma QR trong anh." },
                    onError = { errorMessage = it },
                )
            }
        },
    )

    Scaffold(
        containerColor = colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quet ma QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lai")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                if (!hasCameraPermission) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Can quyen Camera de quet QR.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Cap quyen Camera")
                        }
                    }
                } else {
                    CameraQrPreview(
                        isEnabled = isScanningEnabled,
                        onQrFound = { raw ->
                            isScanningEnabled = false
                            onScanned(raw)
                        },
                        onError = { errorMessage = it },
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                    )
                }
            }

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(52.dp),
                    onClick = { pickImageLauncher.launch("image/*") },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Chon anh",
                        )
                    }
                }

                TextButton(onClick = onManualInput) {
                    Text("Nhap ma thu cong")
                }
            }
        }
    }
}

@Composable
private fun CameraQrPreview(
    isEnabled: Boolean,
    onQrFound: (String) -> Unit,
    onError: (String) -> Unit,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
) {
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        // no-op; binding happens in AndroidView factory/update
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).also { pv ->
                pv.scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = pv
            }
        },
        update = { pv ->
            if (!isEnabled) return@AndroidView

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }

                    val executor = Executors.newSingleThreadExecutor()
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(executor) { imageProxy ->
                        analyzeFrameForQr(
                            imageProxy = imageProxy,
                            scanner = scanner,
                            onQrFound = onQrFound,
                            onError = onError,
                        )
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis,
                        )
                    } catch (t: Throwable) {
                        onError("Khong the mo camera: ${t.message ?: t::class.java.simpleName}")
                    }
                },
                ContextCompat.getMainExecutor(context),
            )
        },
    )
}

private fun analyzeFrameForQr(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrFound: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val raw = barcodes.firstOrNull()?.rawValue
            if (!raw.isNullOrBlank()) {
                onQrFound(raw)
            }
        }
        .addOnFailureListener { e ->
            onError("Loi quet QR: ${e.message ?: "unknown"}")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun scanQrFromImageUri(
    context: Context,
    uri: Uri,
    onFound: (String) -> Unit,
    onNotFound: () -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val image = InputImage.fromBitmap(bitmap, 0)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val raw = barcodes.firstOrNull()?.rawValue
                if (!raw.isNullOrBlank()) onFound(raw) else onNotFound()
            }
            .addOnFailureListener { e ->
                onError("Loi doc QR tu anh: ${e.message ?: "unknown"}")
            }
    } catch (t: Throwable) {
        onError("Khong the mo anh: ${t.message ?: t::class.java.simpleName}")
    }
}

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

