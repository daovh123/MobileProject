package com.example.mobileproject.presentation.ui.screen.memories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.icons.LucideCamera
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.viewmodel.MemoriesViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@Composable
fun CaptureMomentScreen(
    accessToken: String,
    onClose: () -> Unit,
) {
    val viewModel: MemoriesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            val compressed = compressBitmap(bitmap)
            previewBitmap = BitmapFactory.decodeByteArray(compressed, 0, compressed.size)
            imageBytes = compressed
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    val compressed = compressBitmap(bitmap)
                    previewBitmap = BitmapFactory.decodeByteArray(compressed, 0, compressed.size)
                    imageBytes = compressed
                }
            }
        }
    }

    LaunchedEffect(accessToken) {
        viewModel.load(accessToken)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = LucideClose,
                    contentDescription = stringResource(R.string.memories_locket_close),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (previewBitmap != null) {
                AsyncImage(
                    model = previewBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = LucideCamera,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.memories_locket_capture_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text(stringResource(R.string.memories_locket_title_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { cameraLauncher.launch(null) },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Icon(
                    imageVector = LucideCamera,
                    contentDescription = stringResource(R.string.memories_locket_capture),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Icon(
                    imageVector = LucideImage,
                    contentDescription = stringResource(R.string.memories_locket_gallery),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val bytes = imageBytes ?: return@Button
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val payload = "data:image/jpeg;base64,$base64"
                    scope.launch {
                        val success = viewModel.saveMoment(title, payload)
                        if (success) {
                            onClose()
                        }
                    }
                },
                enabled = imageBytes != null && !uiState.isSaving,
            ) {
                Text(text = stringResource(R.string.memories_locket_save))
            }
        }
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
