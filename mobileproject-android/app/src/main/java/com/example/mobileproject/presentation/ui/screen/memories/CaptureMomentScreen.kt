package com.example.mobileproject.presentation.ui.screen.memories

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.icons.LucideChevronRight
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.ui.icons.LucideRefreshCw
import com.example.mobileproject.presentation.viewmodel.MemoriesViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CaptureMomentScreen(
    accessToken: String,
    onClose: () -> Unit,
) {
    val viewModel: MemoriesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    var showMomentGrid by rememberSaveable { mutableStateOf(false) }
    var lensFacing by rememberSaveable { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Bạn cần cấp quyền camera để chụp ảnh.", Toast.LENGTH_SHORT).show()
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val sortedMoments = remember(uiState.moments) { sortMomentsNewestFirst(uiState.moments) }

    LaunchedEffect(accessToken) {
        viewModel.load(accessToken)
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    DisposableEffect(hasCameraPermission, lensFacing, lifecycleOwner) {
        if (!hasCameraPermission) {
            imageCapture = null
            onDispose {}
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val listener = Runnable {
                val provider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                val selector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                    imageCapture = capture
                } catch (ex: Exception) {
                    imageCapture = null
                    Toast.makeText(context, "Không thể khởi động camera", Toast.LENGTH_SHORT).show()
                }
            }
            cameraProviderFuture.addListener(listener, mainExecutor)

            onDispose {
                runCatching {
                    cameraProviderFuture.get().unbindAll()
                }
                imageCapture = null
            }
        }
    }

    AppScreenBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(bottom = 18.dp, top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = LucideClose,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.82f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Chưa có quyền camera",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Text("Cấp quyền")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { showMomentGrid = true },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    ) {
                        Icon(
                            imageVector = LucideImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(88.dp)
                            .clickable(enabled = !uiState.isSaving && hasCameraPermission && imageCapture != null) {
                                val capture = imageCapture
                                if (capture == null) {
                                    Toast.makeText(context, "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                val outputFile = File(context.cacheDir, "moment_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                                capture.takePicture(
                                    outputOptions,
                                    mainExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            val bytes = runCatching {
                                                val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                                                if (bitmap != null) compressBitmap(bitmap) else outputFile.readBytes()
                                            }.getOrElse { outputFile.readBytes() }
                                            runCatching { outputFile.delete() }

                                            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                            val payload = "data:image/jpeg;base64,$base64"
                                            val title = "Khoảnh khắc ${DateTimeFormatter.ofPattern("HH:mm dd/MM").format(LocalDateTime.now())}"

                                            scope.launch {
                                                val success = viewModel.saveMoment(title = title, base64Image = payload)
                                                if (success) {
                                                    viewModel.refreshMoments(showLoading = false)
                                                    Toast.makeText(context, "Đã lưu khoảnh khắc", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Toast.makeText(context, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier.size(76.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.onPrimary,
                            ) {}
                        }
                    }

                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    ) {
                        Icon(
                            imageVector = LucideRefreshCw,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = "Lịch sử",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            if (sortedMoments.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có ảnh kỷ niệm nào.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            } else {
                items(sortedMoments) { moment ->
                    HistoryMomentItem(moment = moment)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = LucideChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }

    if (showMomentGrid) {
        MomentGridBottomSheet(
            moments = sortedMoments,
            onDismiss = { showMomentGrid = false },
        )
    }
}

@Composable
private fun HistoryMomentItem(moment: MomentDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            ) {
                MomentPhoto(model = moment.imageUrl, contentDescription = moment.title)
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = moment.title.ifBlank { "Khoảnh khắc" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!moment.createdAt.isNullOrBlank()) {
                    Text(
                        text = formatMomentDateText(moment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MomentGridBottomSheet(
    moments: List<MomentDto>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Tất cả khoảnh khắc",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            if (moments.isEmpty()) {
                Text(
                    text = "Chưa có ảnh.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(moments) { moment ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            MomentPhoto(model = moment.imageUrl, contentDescription = moment.title)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MomentPhoto(
    model: String,
    contentDescription: String?,
) {
    var imageModel by remember(model) { mutableStateOf<Any?>(null) }

    LaunchedEffect(model) {
        if (model.startsWith("data:")) {
            val decoded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                decodeDataUrl(model)
            }
            imageModel = decoded
        } else {
            imageModel = model
        }
    }

    if (imageModel != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageModel)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
    }
}

private fun sortMomentsNewestFirst(moments: List<MomentDto>): List<MomentDto> {
    return moments.sortedByDescending { parseMomentEpochMillis(it.createdAt) ?: Long.MIN_VALUE }
}

private fun parseMomentEpochMillis(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    val text = raw.trim()
    return runCatching {
        when {
            text.endsWith("Z") || text.contains("+") -> Instant.parse(text).toEpochMilli()
            else -> {
                val local = LocalDateTime.parse(text.replace(" ", "T").substringBefore('.'))
                local.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
    }.getOrNull()
}

private fun formatMomentDateText(raw: String): String {
    val epoch = parseMomentEpochMillis(raw) ?: return raw
    val dateTime = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("HH:mm • dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
    return dateTime.format(formatter)
}

private fun decodeDataUrl(dataUrl: String): ByteArray? {
    if (!dataUrl.startsWith("data:")) return null
    val commaIndex = dataUrl.indexOf(',')
    if (commaIndex == -1 || commaIndex == dataUrl.lastIndex) return null
    val base64 = dataUrl.substring(commaIndex + 1)
    return try {
        Base64.decode(base64, Base64.DEFAULT)
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun compressBitmap(bitmap: Bitmap): ByteArray {
    val maxWidth = 1080f
    val ratio = (maxWidth / bitmap.width).coerceAtMost(1f)
    val scaled = if (ratio < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).roundToInt(),
            (bitmap.height * ratio).roundToInt(),
            true,
        )
    } else {
        bitmap
    }

    val outputStream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
    return outputStream.toByteArray()
}
