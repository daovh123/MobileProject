package com.example.mobileproject.presentation.ui.screen.memories

import android.util.Base64
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.mobileproject.R
import com.example.mobileproject.data.model.moment.MomentCommentDto
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.icons.LucideCamera
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.ui.icons.LucideReply
import com.example.mobileproject.presentation.ui.icons.LucideSend
import com.example.mobileproject.presentation.viewmodel.MemoriesViewModel

private const val REACTION_HEART = "HEART"

@Composable
fun LocketMemoriesScreen(
    accessToken: String,
    onOpenCapture: () -> Unit,
) {
    val viewModel: MemoriesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var commentDraft by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(accessToken) {
        viewModel.load(accessToken)
    }

    DisposableEffect(lifecycleOwner, accessToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.load(accessToken)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    AppScreenBackground {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val latestMoment = uiState.moments.firstOrNull()
            val otherMoments = if (uiState.moments.size > 1) uiState.moments.drop(1) else emptyList()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppSectionHeader(
                            title = stringResource(R.string.memories_locket_title),
                            subtitle = stringResource(R.string.memories_locket_subtitle),
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = onOpenCapture) {
                            Icon(
                                imageVector = LucideCamera,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.memories_locket_capture))
                        }
                    }
                }

                item {
                    if (latestMoment == null) {
                        EmptyMomentCard(onOpenCapture = onOpenCapture)
                    } else {
                        MomentHeroCard(
                            moment = latestMoment,
                            onReact = { viewModel.toggleReaction(latestMoment.id.orEmpty(), REACTION_HEART) },
                            onOpenComments = { viewModel.openComments(latestMoment.id.orEmpty()) },
                        )
                    }
                }

                if (otherMoments.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.memories_locket_recent_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    items(otherMoments) { moment ->
                        MomentListCard(
                            moment = moment,
                            onReact = { viewModel.toggleReaction(moment.id.orEmpty(), REACTION_HEART) },
                            onOpenComments = { viewModel.openComments(moment.id.orEmpty()) },
                        )
                    }
                }
            }
        }
    }

    if (uiState.isCommentsVisible) {
        CommentsBottomSheet(
            comments = uiState.comments,
            isLoading = uiState.isCommentsLoading,
            draft = commentDraft,
            onDraftChange = { commentDraft = it },
            onDismiss = {
                commentDraft = ""
                viewModel.dismissComments()
            },
            onSend = {
                viewModel.submitComment(commentDraft)
                commentDraft = ""
            }
        )
    }
}

@Composable
private fun EmptyMomentCard(
    onOpenCapture: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.memories_locket_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.memories_locket_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenCapture) {
                Text(text = stringResource(R.string.memories_locket_capture))
            }
        }
    }
}

@Composable
private fun MomentHeroCard(
    moment: MomentDto,
    onReact: () -> Unit,
    onOpenComments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageData = remember(moment.imageUrl) { decodeDataUrl(moment.imageUrl) }
    val imageModel = imageData ?: moment.imageUrl
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageModel)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .apply {
                            moment.id?.let {
                                memoryCacheKey("moment_$it")
                                diskCacheKey("moment_$it")
                            }
                        }
                        .build(),
                    contentDescription = moment.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(
                            text = moment.title.ifBlank { stringResource(R.string.memories_locket_title_fallback) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!moment.createdAt.isNullOrBlank()) {
                            Text(
                                text = moment.createdAt.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            ReactionRow(
                reactionsCount = moment.reactionsCount,
                commentsCount = moment.commentsCount,
                isReacted = moment.viewerReaction == REACTION_HEART,
                onReact = onReact,
                onOpenComments = onOpenComments,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun MomentListCard(
    moment: MomentDto,
    onReact: () -> Unit,
    onOpenComments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageData = remember(moment.imageUrl) { decodeDataUrl(moment.imageUrl) }
    val imageModel = imageData ?: moment.imageUrl
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageModel)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .apply {
                        moment.id?.let {
                            memoryCacheKey("moment_$it")
                            diskCacheKey("moment_$it")
                        }
                    }
                    .build(),
                contentDescription = moment.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = moment.title.ifBlank { stringResource(R.string.memories_locket_title_fallback) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (!moment.createdAt.isNullOrBlank()) {
                    Text(
                        text = moment.createdAt.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ReactionRow(
                    reactionsCount = moment.reactionsCount,
                    commentsCount = moment.commentsCount,
                    isReacted = moment.viewerReaction == REACTION_HEART,
                    onReact = onReact,
                    onOpenComments = onOpenComments,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ReactionRow(
    reactionsCount: Int,
    commentsCount: Int,
    isReacted: Boolean,
    onReact: () -> Unit,
    onOpenComments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onReact) {
            Icon(
                imageVector = LucideHeart,
                contentDescription = stringResource(R.string.memories_locket_react),
                tint = if (isReacted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = reactionsCount.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = onOpenComments) {
            Icon(
                imageVector = LucideReply,
                contentDescription = stringResource(R.string.memories_locket_comments),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = commentsCount.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun decodeDataUrl(dataUrl: String): ByteArray? {
    if (!dataUrl.startsWith("data:")) {
        return null
    }
    val commaIndex = dataUrl.indexOf(',')
    if (commaIndex == -1 || commaIndex == dataUrl.lastIndex) {
        return null
    }
    val base64 = dataUrl.substring(commaIndex + 1)
    return try {
        Base64.decode(base64, Base64.DEFAULT)
    } catch (ex: IllegalArgumentException) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    comments: List<MomentCommentDto>,
    isLoading: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.memories_locket_comments),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (comments.isEmpty()) {
                Text(
                    text = stringResource(R.string.memories_locket_comments_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(comments) { comment ->
                        CommentRow(comment = comment)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    placeholder = { Text(stringResource(R.string.memories_locket_comment_hint)) },
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSend, enabled = draft.isNotBlank()) {
                    Icon(
                        imageVector = LucideSend,
                        contentDescription = stringResource(R.string.memories_locket_send),
                        tint = if (draft.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: MomentCommentDto, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = comment.authorUsername.ifBlank { stringResource(R.string.memories_locket_comment_author_fallback) },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
