package com.example.mobileproject.presentation.ui.screen.memories

import android.Manifest
import android.util.LruCache
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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
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
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
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

private val captureMomentImageCache = LruCache<String, ByteArray>(24)

private data class CapturedMomentDraft(
    val previewBytes: ByteArray,
    val base64Image: String,
    val suggestedTitle: String,
)

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
    var momentGridScrollTarget by rememberSaveable { mutableStateOf<Int?>(null) }
    var lensFacing by rememberSaveable { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val listState = rememberLazyListState()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedDraft by remember { mutableStateOf<CapturedMomentDraft?>(null) }
    var draftTitle by rememberSaveable { mutableStateOf("") }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
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

    fun discardDraft() {
        capturedDraft = null
        draftTitle = ""
    }

    LaunchedEffect(accessToken) {
        viewModel.ensureLoaded(accessToken)
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
                } catch (_: Exception) {
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

    LaunchedEffect(momentGridScrollTarget) {
        val target = momentGridScrollTarget ?: return@LaunchedEffect
        momentGridScrollTarget = null
        val offset = if (sortedMoments.isNotEmpty()) 4 else 3
        listState.animateScrollToItem(offset + target)
    }

    AppScreenBackground {
        LazyColumn(
            state = listState,
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
                    IconButton(
                        onClick = {
                            if (uiState.isSaving) return@IconButton
                            if (capturedDraft != null) {
                                discardDraft()
                            } else {
                                onClose()
                            }
                        },
                    ) {
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

            if (capturedDraft == null) {
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

                                                if (bytes.isEmpty()) {
                                                    Toast.makeText(context, "Không đọc được ảnh vừa chụp", Toast.LENGTH_SHORT).show()
                                                    return
                                                }

                                                val suggestedTitle = buildSuggestedMomentTitle(LocalDateTime.now())
                                                capturedDraft = CapturedMomentDraft(
                                                    previewBytes = bytes,
                                                    base64Image = "data:image/jpeg;base64,${
                                                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                                                    }",
                                                    suggestedTitle = suggestedTitle,
                                                )
                                                draftTitle = suggestedTitle
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                Toast.makeText(context, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show()
                                            }
                                        },
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
                    itemsIndexed(
                        items = sortedMoments,
                        key = { index, moment -> moment.id ?: "${moment.createdAt}_${index}" },
                    ) { index, moment ->
                        HistoryMomentCard(
                            moment = moment,
                            order = index + 1,
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            } else {
                item {
                    CaptureMomentReviewHeader()
                }

                item {
                    CaptureMomentReviewCard(
                        previewBytes = capturedDraft!!.previewBytes,
                        title = draftTitle,
                        onTitleChange = { draftTitle = it },
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            onClick = {
                                if (!uiState.isSaving) {
                                    discardDraft()
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Hủy bỏ",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val draft = capturedDraft ?: return@Button
                                val finalTitle = draftTitle.trim().ifBlank { draft.suggestedTitle }
                                scope.launch {
                                    val success = viewModel.saveMoment(
                                        title = finalTitle,
                                        base64Image = draft.base64Image,
                                    )
                                    if (success) {
                                        discardDraft()
                                        Toast.makeText(context, "Đã lưu khoảnh khắc", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text(
                                text = if (uiState.isSaving) "Đang lưu..." else "Lưu khoảnh khắc",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMomentGrid && capturedDraft == null) {
        MomentGridBottomSheet(
            moments = sortedMoments,
            onDismiss = { showMomentGrid = false },
            onMomentClick = { index ->
                showMomentGrid = false
                momentGridScrollTarget = index
            },
        )
    }
}

@Composable
private fun CaptureMomentReviewHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Xác nhận khoảnh khắc",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Xem lại ảnh vừa chụp, chỉnh title nếu muốn rồi lưu hoặc hủy bỏ.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CaptureMomentReviewCard(
    previewBytes: ByteArray,
    title: String,
    onTitleChange: (String) -> Unit,
) {
    val previewBitmap = remember(previewBytes) {
        BitmapFactory.decodeByteArray(previewBytes, 0, previewBytes.size)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Không xem trước được ảnh",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title khoảnh khắc") },
                placeholder = { Text("Nhập title bạn muốn lưu") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun HistoryMomentCard(
    moment: MomentDto,
    order: Int,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isEven = order % 2 == 0
    val accentContainer = if (isEven) {
        colorScheme.tertiaryContainer.copy(alpha = 0.78f)
    } else {
        colorScheme.primaryContainer.copy(alpha = 0.78f)
    }
    val accentColor = if (isEven) colorScheme.tertiary else colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            ) {
                MomentPhoto(model = moment.imageUrl, contentDescription = moment.title)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.scrim.copy(alpha = 0.0f),
                                    colorScheme.scrim.copy(alpha = 0.05f),
                                    colorScheme.scrim.copy(alpha = 0.35f),
                                ),
                            ),
                        ),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    MomentMetaChip(
                        text = "#${order.toString().padStart(2, '0')}",
                        containerColor = accentContainer,
                        contentColor = accentColor,
                    )
                    moment.createdAt?.takeIf { it.isNotBlank() }?.let {
                        MomentMetaChip(
                            text = formatMomentDateText(it),
                            containerColor = colorScheme.surface.copy(alpha = 0.88f),
                            contentColor = colorScheme.onSurface,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = moment.title.ifBlank { "Khoảnh khắc" },
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (moment.commentsCount > 0) {
                        MomentMetaChip(
                            text = "${moment.commentsCount} bình luận",
                            containerColor = colorScheme.surfaceContainerHigh,
                            contentColor = colorScheme.onSurfaceVariant,
                        )
                    }
                    if (moment.reactionsCount > 0) {
                        MomentMetaChip(
                            text = "${moment.reactionsCount} phản ứng",
                            containerColor = colorScheme.surfaceContainerHigh,
                            contentColor = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MomentMetaChip(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MomentGridBottomSheet(
    moments: List<MomentDto>,
    onDismiss: () -> Unit,
    onMomentClick: (Int) -> Unit,
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
                            modifier = Modifier.aspectRatio(1f),
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
    val context = LocalContext.current
    var shouldStartLoad by remember(model) { mutableStateOf(false) }
    val imageModel by androidx.compose.runtime.produceState<Any?>(initialValue = null, model, shouldStartLoad) {
        if (!shouldStartLoad) return@produceState

        value = if (model.startsWith("data:")) {
            captureMomentImageCache.get(model) ?: kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                decodeDataUrl(model)
            }?.also { decoded ->
                captureMomentImageCache.put(model, decoded)
            }
        } else {
            model
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                if (!shouldStartLoad) {
                    shouldStartLoad = true
                }
            },
    ) {
        if (imageModel != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageModel)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { MomentPhotoPlaceholder() },
                error = { MomentPhotoPlaceholder() },
            )
        } else {
            MomentPhotoPlaceholder()
        }
    }
}

@Composable
private fun MomentPhotoPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LucideImage,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
        )
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

private fun buildSuggestedMomentTitle(now: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm", Locale.forLanguageTag("vi-VN"))
    return "Khoảnh khắc ${formatter.format(now)}"
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
