package com.example.mobileproject.presentation.ui.screen.memories

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.icons.LucideCamera
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.ui.icons.LucideReply
import com.example.mobileproject.presentation.ui.icons.LucideSend
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.viewmodel.MemoriesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

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
    var showMomentsHistory by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(accessToken) {
        viewModel.load(accessToken)
    }

    DisposableEffect(lifecycleOwner, accessToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshMoments(showLoading = false)
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
            val sortedMoments = remember(uiState.moments) { sortMomentsNewestFirst(uiState.moments) }
            val latestMoment = sortedMoments.firstOrNull()
            val otherMoments = if (sortedMoments.size > 1) sortedMoments.drop(1) else emptyList()
            val nearestReminder = remember(uiState.relationshipStartAt) {
                buildUpcomingCalendarReminders(
                    relationshipStartAt = uiState.relationshipStartAt,
                    today = LocalDate.now(),
                ).firstOrNull()
            }

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
                    PartnerInfoCard(
                        partnerProfile = uiState.partnerProfile,
                        isLoading = uiState.isPartnerInfoLoading && uiState.isPartnerInfoExpanded,
                        onClick = { viewModel.onPartnerInfoClick() },
                    )
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

                item {
                    RecentMomentsCard(
                        moments = otherMoments,
                        onOpenCapture = onOpenCapture,
                        onOpenHistory = { showMomentsHistory = true },
                    )
                }

                item {
                    MemoriesCalendarCard(nearestReminder = nearestReminder)
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

    if (uiState.isPartnerInfoExpanded) {
        PartnerInfoOverlay(
            partnerProfile = uiState.partnerProfile,
            isLoading = uiState.isPartnerInfoLoading,
            onDismiss = { viewModel.onPartnerInfoClick() },
        )
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

    if (showMomentsHistory) {
        MomentsHistoryBottomSheet(
            moments = sortedMomentsForSheet(uiState.moments),
            onDismiss = { showMomentsHistory = false },
            onCreateNew = {
                showMomentsHistory = false
                onOpenCapture()
            },
        )
    }
}

@Composable
private fun PartnerInfoCard(
    partnerProfile: PartnerProfileSummary?,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.memories_partner_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isLoading) {
                    stringResource(R.string.memories_partner_loading)
                } else {
                    partnerProfile?.fullName?.takeIf { it.isNotBlank() }
                        ?: partnerProfile?.nickName?.takeIf { it.isNotBlank() }
                        ?: partnerProfile?.username?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.memories_partner_info_hint)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PartnerInfoOverlay(
    partnerProfile: PartnerProfileSummary?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.66f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.memories_partner_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = LucideClose,
                            contentDescription = null,
                        )
                    }
                }

                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.memories_partner_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    return@Column
                }

                if (partnerProfile == null || !partnerProfile.paired) {
                    Text(
                        text = partnerProfile?.message ?: stringResource(R.string.memories_partner_not_paired),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    return@Column
                }

                val displayName = partnerProfile.fullName
                    ?.takeIf { it.isNotBlank() }
                    ?: partnerProfile.nickName?.takeIf { it.isNotBlank() }
                    ?: partnerProfile.username?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.memories_partner_unknown)

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                partnerProfile.username?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = "@$it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                partnerProfile.daysTogether?.let {
                    Text(
                        text = stringResource(R.string.memories_partner_days_together, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                partnerProfile.startAt?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = stringResource(R.string.memories_partner_start_at, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                AsyncImage(
                    model = buildMomentImageRequest(
                        context = LocalContext.current,
                        rawModel = moment.imageUrl,
                        cacheKey = moment.id?.let { "moment_$it" },
                    ),
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            AsyncImage(
                model = buildMomentImageRequest(
                    context = LocalContext.current,
                    rawModel = moment.imageUrl,
                    cacheKey = moment.id?.let { "moment_$it" },
                ),
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

@Composable
private fun RecentMomentsCard(
    moments: List<MomentDto>,
    onOpenCapture: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val latestMoment = moments.firstOrNull()
    val secondMoment = moments.getOrNull(1)
    val remainingCount = if (moments.size > 2) moments.size - 2 else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenHistory),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E8EA)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.memories_locket_recent_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B3A3D),
                )
                Icon(
                    imageVector = LucideImage,
                    contentDescription = null,
                    tint = Color(0xFFF28A97),
                    modifier = Modifier.size(18.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier.weight(1f).height(126.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (latestMoment != null) {
                            MomentPhoto(model = latestMoment.imageUrl, contentDescription = latestMoment.title)
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF28A97),
                            ) {
                                Text(
                                    text = latestMoment.title.ifBlank { stringResource(R.string.memories_locket_title_fallback) },
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        } else {
                            EmptyMomentTile(onOpenCapture = onOpenCapture)
                        }
                    }
                }

                Column(modifier = Modifier.width(78.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(59.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        if (secondMoment != null) {
                            MomentPhoto(model = secondMoment.imageUrl, contentDescription = secondMoment.title)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = LucideCamera,
                                    contentDescription = null,
                                    tint = Color(0xFFF28A97),
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().height(59.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF28A97)),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clickable(onClick = if (moments.isEmpty()) onOpenCapture else onOpenHistory),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (remainingCount > 0) "+$remainingCount" else stringResource(R.string.memories_locket_capture),
                                color = Color.White,
                                style = if (remainingCount > 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MomentsHistoryBottomSheet(
    moments: List<MomentDto>,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Toàn bộ kỷ niệm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A2A2D),
            )

            Button(
                onClick = onCreateNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF28A97),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = LucideCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo kỷ niệm mới", fontWeight = FontWeight.SemiBold)
            }

            if (moments.isEmpty()) {
                Text(
                    text = "Chưa có kỷ niệm nào.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(moments) { moment ->
                        MomentHistoryItem(moment = moment)
                    }
                }
            }
        }
    }
}

@Composable
private fun MomentHistoryItem(moment: MomentDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                MomentPhoto(model = moment.imageUrl, contentDescription = moment.title)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moment.title.ifBlank { stringResource(R.string.memories_locket_title_fallback) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = moment.createdAt?.let { formatMomentDateText(it) } ?: "Không rõ thời gian",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MemoriesCalendarCard(
    nearestReminder: CalendarReminder?,
) {
    val today = remember { LocalDate.now() }
    val calendarMonth = remember { YearMonth.now() }
    val selectedDay = when {
        nearestReminder != null && YearMonth.from(nearestReminder.date) == calendarMonth -> nearestReminder.date.dayOfMonth
        else -> today.dayOfMonth
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7E9EB)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tháng ${calendarMonth.monthValue}/${calendarMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B3A3D),
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF8E777B),
                    )
                }

                CalendarMonthGrid(month = calendarMonth, selectedDay = selectedDay)

                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF3EFF0)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(modifier = Modifier.size(28.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFFFFD8DE)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Cake,
                                    contentDescription = null,
                                    tint = Color(0xFFF28A97),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nearestReminder?.title ?: "Kỷ niệm gần nhất",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4B3A3D),
                            )
                            val subtitle = nearestReminder?.let { reminder ->
                                val days = ChronoUnit.DAYS.between(today, reminder.date).coerceAtLeast(0)
                                "Còn $days ngày • ${formatReminderDate(reminder.date)}"
                            } ?: "Chưa có dữ liệu ngày bắt đầu"
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8E777B),
                            )
                        }
                    }
                }
            }

            Image(
                painter = painterResource(R.drawable.sticker_7),
                contentDescription = null,
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-6).dp),
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDay: Int,
) {
    val weekdays = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    val firstDay = month.atDay(1)
    val leadingBlank = firstDay.dayOfWeek.value % 7
    val days = (1..month.lengthOfMonth()).toList()
    val cells = List(leadingBlank) { 0 } + days
    val rows = (cells + List((7 - (cells.size % 7)) % 7) { 0 }).chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFAA969A),
                    modifier = Modifier.width(28.dp),
                )
            }
        }

        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { day ->
                    if (day == 0) {
                        Spacer(modifier = Modifier.width(28.dp).height(28.dp))
                    } else {
                        val isSelected = day == selectedDay
                        Surface(
                            modifier = Modifier.width(28.dp).height(28.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFFF28A97) else Color.Transparent,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) Color.White else Color(0xFF4B3A3D),
                                )
                            }
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
    AsyncImage(
        model = buildMomentImageRequest(
            context = LocalContext.current,
            rawModel = model,
            cacheKey = model.takeIf { it.isNotBlank() }?.hashCode()?.toString(),
        ),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun EmptyMomentTile(
    onOpenCapture: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize().clickable(onClick = onOpenCapture),
        color = Color(0xFFFFEEF1),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.memories_locket_capture_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF28A97),
            )
        }
    }
}

private data class CalendarReminder(
    val date: LocalDate,
    val title: String,
)

private fun buildUpcomingCalendarReminders(
    relationshipStartAt: String?,
    today: LocalDate,
): List<CalendarReminder> {
    val reminders = mutableListOf<CalendarReminder>()
    val thisYear = today.year

    listOf(
        CalendarReminder(LocalDate.of(thisYear, 2, 14), "Valentine"),
        CalendarReminder(LocalDate.of(thisYear, 3, 8), "Quốc tế Phụ nữ 8/3"),
        CalendarReminder(LocalDate.of(thisYear + 1, 2, 14), "Valentine"),
        CalendarReminder(LocalDate.of(thisYear + 1, 3, 8), "Quốc tế Phụ nữ 8/3"),
    ).filterTo(reminders) { !it.date.isBefore(today) }

    val startDate = parseRelationshipStartDate(relationshipStartAt)
    if (startDate != null) {
        val daysTogether = ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(0)
        val firstMilestone = (((daysTogether / 100) + 1) * 100).toInt()
        for (step in 0 until 8) {
            val milestoneDays = firstMilestone + (step * 100)
            reminders += CalendarReminder(
                date = startDate.plusDays(milestoneDays.toLong()),
                title = "Kỷ niệm $milestoneDays ngày bên nhau",
            )
        }

        val yearsTogether = ChronoUnit.YEARS.between(startDate, today).coerceAtLeast(0)
        val firstYear = (yearsTogether + 1).toInt()
        for (step in 0 until 5) {
            val milestoneYear = firstYear + step
            reminders += CalendarReminder(
                date = startDate.plusYears(milestoneYear.toLong()),
                title = "Kỷ niệm $milestoneYear năm bên nhau",
            )
        }
    }

    return reminders
        .distinctBy { it.date to it.title }
        .filter { !it.date.isBefore(today) }
        .sortedBy { it.date }
}

private fun sortMomentsNewestFirst(moments: List<MomentDto>): List<MomentDto> {
    return moments.sortedByDescending { parseMomentEpochMillis(it.createdAt) ?: Long.MIN_VALUE }
}

private fun sortedMomentsForSheet(moments: List<MomentDto>): List<MomentDto> {
    return sortMomentsNewestFirst(moments)
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
    val epoch = parseMomentEpochMillis(raw)
    if (epoch == null) return raw
    val dateTime = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val dateFormatter = DateTimeFormatter.ofPattern("HH:mm • dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
    return dateTime.format(dateFormatter)
}

private fun parseRelationshipStartDate(raw: String?): LocalDate? {
    if (raw.isNullOrBlank()) return null
    val trimmed = raw.trim()
    val datePart = if (trimmed.length >= 10) trimmed.substring(0, 10) else trimmed
    return runCatching { LocalDate.parse(datePart) }.getOrNull()
}

private fun formatReminderDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")))
}

private fun buildMomentImageRequest(
    context: android.content.Context,
    rawModel: String,
    cacheKey: String?,
): ImageRequest {
    return ImageRequest.Builder(context)
        .data(rawModel)
        .crossfade(false)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .apply {
            cacheKey?.let {
                memoryCacheKey(it)
                diskCacheKey(it)
            }
        }
        .build()
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
