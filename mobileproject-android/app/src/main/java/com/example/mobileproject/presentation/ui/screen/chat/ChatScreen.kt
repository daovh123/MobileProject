package com.example.mobileproject.presentation.ui.screen.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.ChatMessage
import com.example.mobileproject.domain.entity.ReadStatus
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideRefreshCw
import com.example.mobileproject.presentation.ui.icons.LucideReply
import com.example.mobileproject.presentation.ui.icons.LucideSend
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.viewmodel.ChatViewModel
import com.example.mobileproject.presentation.viewmodel.PendingMessage
import com.example.mobileproject.presentation.viewmodel.SendStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun decodeBase64Avatar(dataUrl: String?): Bitmap? {
    if (dataUrl.isNullOrBlank()) return null
    return runCatching {
        val base64 = dataUrl.substringAfter(",", missingDelimiterValue = "")
        val bytes = if (base64.isBlank()) Base64.decode(dataUrl.trim(), Base64.DEFAULT)
                    else Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()
}

@Composable
fun ChatScreen(
    accessToken: String,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var inputValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    LaunchedEffect(accessToken) {
        viewModel.start(accessToken)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(uiState.messages.size) {
        val lastIndex = uiState.messages.lastIndex
        if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
    }

    // Resolve partner avatar: prefer top-level field from chat_history,
    // fall back to the most recent senderAvatarUrl from a non-mine message.
    val resolvedPartnerAvatarUrl = uiState.partnerAvatarUrl
        ?: uiState.messages.lastOrNull { !it.mine && !it.senderAvatarUrl.isNullOrBlank() }?.senderAvatarUrl

    val partnerBitmap = remember(resolvedPartnerAvatarUrl) {
        decodeBase64Avatar(resolvedPartnerAvatarUrl)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        // ── TopBar ──────────────────────────────────────────────────
        // Derive display name: prefer server-provided partnerName, else
        // the username portion before '@' (e.g. "thaiviethoang2910" from email).
        val partnerRawUsername = uiState.messages.firstOrNull { !it.mine }?.senderUsername
        val rawPartnerName = uiState.partnerName ?: partnerRawUsername
        val displayPartnerName = rawPartnerName?.substringBefore("@")?.takeIf { it.isNotBlank() } ?: "Partner"
        // Initial letter for fallback avatar (always available from username)
        val partnerInitial = displayPartnerName.firstOrNull()?.toString()
            ?: partnerRawUsername?.firstOrNull()?.toString()

        ChatTopBar(
            partnerName = displayPartnerName,
            partnerBitmap = partnerBitmap,
            partnerInitial = partnerInitial,
            isTyping = uiState.isPartnerTyping,
        )

        // ── Message list ────────────────────────────────────────────
        val messages = uiState.messages
        if (messages.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.chat_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            val groupedMessages = remember(messages) { groupMessages(messages) }
            val dateGrouped = remember(groupedMessages) { insertDateSeparators(groupedMessages) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(items = dateGrouped, key = { it.key }) { item ->
                    when (item) {
                        is ChatListItem.DateLabel -> DateSeparator(label = item.label)
                        is ChatListItem.MessageItem -> SwipeableReplyBubble(
                            message = item.grouped.message,
                            showAvatar = item.grouped.showAvatar,
                            showTimestamp = item.grouped.showTimestamp,
                            allMessages = messages,
                            partnerBitmap = partnerBitmap,
                            partnerInitial = partnerInitial,
                            partnerName = displayPartnerName,
                            onSwipeToReply = { viewModel.setReplyTo(it) },
                        )
                    }
                }
            }
        }

        // ── Pending messages ────────────────────────────────────────
        val pendingMessages = uiState.pendingMessages
        if (pendingMessages.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                pendingMessages.forEach { pending ->
                    PendingMessageBubble(
                        pending = pending,
                        onRetry = { viewModel.retry(pending.localId) },
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        // ── Typing indicator ────────────────────────────────────────
        if (uiState.isPartnerTyping) {
            TypingIndicatorBubble(modifier = Modifier.padding(horizontal = 12.dp))
        }

        // ── Error ───────────────────────────────────────────────────
        val errorMessage = uiState.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        // ── Reply quote bar ─────────────────────────────────────────
        val replyingTo = uiState.replyingToMessage
        if (replyingTo != null) {
            ReplyQuoteBar(
                message = replyingTo,
                onCancel = { viewModel.setReplyTo(null) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        // ── @MiniAI suggestion ──────────────────────────────────────
        val mentionContext = findMentionContext(inputValue)
        val showMiniAiSuggestion = mentionContext != null && shouldSuggestMiniAi(mentionContext.query)
        if (showMiniAiSuggestion) {
            MiniAiSuggestionRow(
                onClick = {
                    mentionContext?.let {
                        inputValue = completeMention(
                            value = inputValue,
                            tokenStart = it.start,
                            tokenEnd = it.end,
                            mentionText = "@MiniAI ",
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        // ── Input bar ───────────────────────────────────────────────
        ChatInputBar(
            value = inputValue,
            onValueChange = {
                inputValue = it
                if (it.text.isNotBlank()) viewModel.sendTypingEvent(accessToken)
            },
            onSend = {
                val toSend = inputValue.text.trim()
                if (toSend.isNotBlank()) {
                    viewModel.send(accessToken, toSend, uiState.replyingToMessage?.id)
                    inputValue = TextFieldValue("")
                }
            },
            isSending = uiState.isSending,
            accessToken = accessToken,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TopBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatTopBar(
    partnerName: String?,
    partnerBitmap: Bitmap?,
    partnerInitial: String?,
    isTyping: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar (no online dot)
            AvatarImage(
                bitmap = partnerBitmap,
                size = 44,
                initial = partnerInitial,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partnerName ?: stringResource(R.string.chat_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isTyping) {
                    Text(
                        text = stringResource(R.string.chat_typing_indicator),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Input bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatInputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    accessToken: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val canSend = !isSending && accessToken.isNotBlank() && value.text.trim().isNotBlank()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.chat_input_placeholder),
                        color = colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = false,
                minLines = 1,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    focusedContainerColor = colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = colorScheme.surfaceContainerLow,
                    cursorColor = colorScheme.primary,
                ),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (canSend) colorScheme.primary else colorScheme.surfaceVariant,
                        shape = CircleShape,
                    )
                    .clickable(enabled = canSend, onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LucideSend,
                    contentDescription = stringResource(R.string.chat_send),
                    tint = if (canSend) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date separator
// ─────────────────────────────────────────────────────────────────────────────

private sealed interface ChatListItem {
    val key: String

    data class DateLabel(val label: String) : ChatListItem {
        override val key: String get() = "date_$label"
    }

    data class MessageItem(val grouped: GroupedMessage) : ChatListItem {
        override val key: String get() = grouped.message.id
    }
}

private fun insertDateSeparators(grouped: List<GroupedMessage>): List<ChatListItem> {
    val result = mutableListOf<ChatListItem>()
    var lastDate: LocalDate? = null
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val yesterday = today.minusDays(1)

    grouped.forEach { g ->
        val millis = parseInstantMillis(g.message.createdAt)
        val msgDate = if (millis > 0) {
            Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
        } else null

        if (msgDate != null && msgDate != lastDate) {
            lastDate = msgDate
            val label = when (msgDate) {
                today -> "Hôm nay"
                yesterday -> "Hôm qua"
                else -> msgDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
            result.add(ChatListItem.DateLabel(label))
        }
        result.add(ChatListItem.MessageItem(g))
    }
    return result
}

@Composable
private fun DateSeparator(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Swipeable bubble wrapper
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableReplyBubble(
    message: ChatMessage,
    showAvatar: Boolean,
    showTimestamp: Boolean,
    allMessages: List<ChatMessage>,
    partnerBitmap: Bitmap?,
    partnerInitial: String?,
    partnerName: String?,
    onSwipeToReply: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) onSwipeToReply(message)
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    imageVector = LucideReply,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    ) {
        ChatBubble(
            message = message,
            showAvatar = showAvatar,
            showTimestamp = showTimestamp,
            allMessages = allMessages,
            partnerBitmap = partnerBitmap,
            partnerInitial = partnerInitial,
            partnerName = partnerName,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chat bubble
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
    showAvatar: Boolean = true,
    showTimestamp: Boolean = true,
    allMessages: List<ChatMessage> = emptyList(),
    partnerBitmap: Bitmap? = null,
    partnerInitial: String? = null,
    partnerName: String? = null,
    modifier: Modifier = Modifier,
) {
    val mine = message.mine
    val colorScheme = MaterialTheme.colorScheme

    val bubbleColor = if (mine) colorScheme.primary else colorScheme.surfaceContainerHigh
    val textColor = if (mine) colorScheme.onPrimary else colorScheme.onSurface
    val timeColor = if (mine) colorScheme.onPrimary.copy(alpha = 0.65f) else colorScheme.onSurfaceVariant

    val timeText = formatTimestamp(message.createdAt)

    val quotedMessage = remember(message.replyToId, allMessages) {
        message.replyToId?.let { id -> allMessages.find { it.id == id } }
    }

    val isAi = !mine && message.senderUsername?.equals("MiniAI", ignoreCase = true) == true

    // Use partnerBitmap (decoded once at screen level) as primary.
    // Only decode per-message avatar as fallback if partnerBitmap is not available.
    val msgAvatarBitmap = when {
        mine || isAi -> null
        partnerBitmap != null -> partnerBitmap
        else -> remember(message.senderAvatarUrl) { decodeBase64Avatar(message.senderAvatarUrl) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!mine) {
            if (showAvatar) {
                AvatarImage(
                    bitmap = if (isAi) null else msgAvatarBitmap,
                    size = 32,
                    initial = if (isAi) "M" else partnerInitial,
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Spacer(modifier = Modifier.width(38.dp))
            }
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
        ) {
            // Only show name label for AI bot — normal partner chat doesn't need it
            if (isAi && showAvatar) {
                Text(
                    text = "MiniAI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                )
            }

            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (mine) 18.dp else if (showAvatar) 4.dp else 18.dp,
                    bottomEnd = if (mine) 4.dp else 18.dp,
                ),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (quotedMessage != null) {
                        QuotedMessagePreview(
                            quoted = quotedMessage,
                            isOnMine = mine,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        lineHeight = 20.sp,
                    )
                    if (showTimestamp && timeText.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = timeColor,
                            )
                            if (mine) {
                                Text(
                                    text = when (message.readStatus) {
                                        ReadStatus.SENT -> "✓"
                                        ReadStatus.DELIVERED -> "✓✓"
                                        ReadStatus.READ -> "✓✓"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (message.readStatus == ReadStatus.READ) colorScheme.onPrimary
                                    else colorScheme.onPrimary.copy(alpha = 0.65f),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (mine) Spacer(modifier = Modifier.width(4.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Avatar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarImage(
    bitmap: Bitmap?,
    size: Int,
    initial: String? = null,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = colorScheme.primaryContainer,
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            !initial.isNullOrBlank() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size * 0.4f).sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colorScheme.onPrimaryContainer,
                )
            }
            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LucideUser,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size((size * 0.5f).dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quoted preview inside bubble
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuotedMessagePreview(
    quoted: ChatMessage,
    isOnMine: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val senderLabel = quoted.senderUsername ?: if (quoted.mine) "Bạn" else "Đối phương"
    val accentColor = if (isOnMine) colorScheme.onPrimary.copy(alpha = 0.8f) else colorScheme.primary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isOnMine) colorScheme.primary.copy(alpha = 0.25f)
                else colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .border(
                width = 2.dp,
                color = accentColor,
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = senderLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            )
            Text(
                text = quoted.text,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOnMine) colorScheme.onPrimary.copy(alpha = 0.75f) else colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reply quote bar (input area)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReplyQuoteBar(
    message: ChatMessage,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val senderLabel = message.senderUsername ?: if (message.mine) "Bạn" else "Đối phương"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceContainerHigh)
            .border(width = 3.dp, color = colorScheme.primary, shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LucideReply,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = senderLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.primary,
            )
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = LucideClose,
                contentDescription = stringResource(R.string.cd_cancel_reply),
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Typing indicator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TypingIndicatorBubble(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(38.dp))
        Surface(
            color = colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(18.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(0, 150, 300).forEachIndexed { index, delay ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 400, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(delay),
                        ),
                        label = "dot$index",
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = colorScheme.onSurfaceVariant.copy(alpha = alpha),
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pending message bubble
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PendingMessageBubble(
    pending: PendingMessage,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isFailed = pending.status == SendStatus.FAILED

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isFailed) {
            IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = LucideRefreshCw,
                    contentDescription = "Thử lại",
                    tint = colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Surface(
            color = if (isFailed) colorScheme.errorContainer else colorScheme.primary.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = pending.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFailed) colorScheme.onErrorContainer else colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isFailed) "Gửi thất bại" else "Đang gửi…",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isFailed) colorScheme.error else colorScheme.onPrimary.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// @MiniAI suggestion
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MiniAiSuggestionRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "MiniAI",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nhấn để chèn @MiniAI",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mention helpers
// ─────────────────────────────────────────────────────────────────────────────

private data class MentionContext(val start: Int, val end: Int, val query: String)

private fun findMentionContext(value: TextFieldValue): MentionContext? {
    val text = value.text
    if (text.isBlank()) return null
    val cursor = value.selection.start.coerceIn(0, text.length)
    val start = run {
        var i = cursor - 1
        while (i >= 0) {
            if (text[i].isWhitespace()) return@run i + 1
            i--
        }
        0
    }
    if (start !in 0..cursor) return null
    val token = text.substring(start, cursor)
    if (!token.startsWith("@")) return null
    return MentionContext(start = start, end = cursor, query = token.drop(1))
}

private fun shouldSuggestMiniAi(query: String): Boolean {
    val q = query.trim().lowercase()
    return q.isEmpty() || "miniai".startsWith(q)
}

private fun completeMention(value: TextFieldValue, tokenStart: Int, tokenEnd: Int, mentionText: String): TextFieldValue {
    val text = value.text
    val safeStart = tokenStart.coerceIn(0, text.length)
    val safeEnd = tokenEnd.coerceIn(safeStart, text.length)
    val newText = buildString {
        append(text.substring(0, safeStart))
        append(mentionText)
        append(text.substring(safeEnd))
    }
    val newCursor = (safeStart + mentionText.length).coerceIn(0, newText.length)
    return value.copy(text = newText, selection = TextRange(newCursor))
}

// ─────────────────────────────────────────────────────────────────────────────
// Timestamp formatter
// ─────────────────────────────────────────────────────────────────────────────

private fun formatTimestamp(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return runCatching {
        val instant = Instant.parse(createdAt.trim())
        val zoneId = ZoneId.systemDefault()
        val localDateTime = instant.atZone(zoneId).toLocalDateTime()
        val today = LocalDate.now(zoneId)
        val messageDate = localDateTime.toLocalDate()
        val timeStr = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        when (messageDate) {
            today -> timeStr
            today.minusDays(1) -> "Hôm qua $timeStr"
            else -> localDateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
        }
    }.getOrDefault(createdAt.trim())
}

