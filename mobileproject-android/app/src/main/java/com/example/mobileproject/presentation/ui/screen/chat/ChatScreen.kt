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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.window.Popup
import androidx.compose.foundation.layout.offset
import androidx.activity.ComponentActivity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.ChatMessage
import com.example.mobileproject.domain.entity.ReadStatus
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideRefreshCw
import com.example.mobileproject.presentation.ui.icons.LucideReply
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.ui.icons.LucideCamera
import com.example.mobileproject.presentation.ui.components.core.AppFullScreenLoading
import com.example.mobileproject.presentation.viewmodel.ChatViewModel
import com.example.mobileproject.presentation.viewmodel.ExplorePlanChatCardParser
import com.example.mobileproject.presentation.viewmodel.ExplorePlanChatCardData
import com.example.mobileproject.presentation.viewmodel.PendingMessage
import com.example.mobileproject.presentation.viewmodel.SendStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val placeImageFallbackPool: List<String> = listOf(
    "https://picsum.photos/seed/cafe-hoa-xa-1/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-2/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-3/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-4/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-5/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-6/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-7/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-8/1200/800",
    "https://picsum.photos/seed/cafe-hoa-xa-9/1200/800",
)

private fun fallbackImageFor(seed: String): String {
    val index = (seed.hashCode() and Int.MAX_VALUE) % placeImageFallbackPool.size
    return placeImageFallbackPool[index]
}

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

    val reactionsMap = remember { mutableStateMapOf<String, String>() }
    var activeReactionMenuMessageId by remember { mutableStateOf<String?>(null) }
    var quickEmoji by rememberSaveable { mutableStateOf("👍") }
    var showEmojiCustomizerDialog by remember { mutableStateOf(false) }

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

    // Find the latest read message from mine to show partner's tiny read avatar
    val lastReadMessageId = remember(uiState.messages) {
        uiState.messages.lastOrNull { it.mine && it.readStatus == com.example.mobileproject.domain.entity.ReadStatus.READ }?.id
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        // ── TopBar ──────────────────────────────────────────────────
        val partnerRawUsername = uiState.messages.firstOrNull { !it.mine }?.senderUsername
        val rawPartnerName = uiState.partnerName ?: partnerRawUsername
        val displayPartnerName = rawPartnerName?.substringBefore("@")?.takeIf { it.isNotBlank() } ?: "Partner"
        val partnerInitial = displayPartnerName.firstOrNull()?.toString()
            ?: partnerRawUsername?.firstOrNull()?.toString()

        ChatTopBar(
            partnerName = displayPartnerName,
            partnerBitmap = partnerBitmap,
            partnerInitial = partnerInitial,
            isTyping = uiState.isPartnerTyping,
            onInfoClick = { showEmojiCustomizerDialog = true },
        )

        // ── Message list ────────────────────────────────────────────
        val messages = uiState.messages
        if (uiState.isLoading && messages.isEmpty()) {
            AppFullScreenLoading(
                modifier = Modifier.weight(1f),
                message = "Đang tải tin nhắn...",
            )
        } else if (messages.isEmpty() && !uiState.isLoading) {
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
                            positionInGroup = item.grouped.positionInGroup,
                            allMessages = messages,
                            partnerBitmap = partnerBitmap,
                            partnerInitial = partnerInitial,
                            partnerName = displayPartnerName,
                            lastReadMessageId = lastReadMessageId,
                            reactionsMap = reactionsMap,
                            activeReactionMenuMessageId = activeReactionMenuMessageId,
                            onReact = { emoji ->
                                reactionsMap[item.grouped.message.id] = emoji
                            },
                            onShowReactionMenu = { show ->
                                activeReactionMenuMessageId = if (show) item.grouped.message.id else null
                            },
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
            onSendLike = {
                viewModel.send(accessToken, quickEmoji, uiState.replyingToMessage?.id)
            },
            quickEmoji = quickEmoji,
            isSending = uiState.isSending,
            accessToken = accessToken,
        )

        if (showEmojiCustomizerDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showEmojiCustomizerDialog = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .width(280.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Thay đổi biểu tượng Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        val customEmojis = listOf("👍", "❤️", "😂", "😮", "🔥", "🎉", "💩", "✨", "💯", "🎈", "😄", "🌟")
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (row in 0 until 4) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (col in 0 until 3) {
                                        val idx = row * 3 + col
                                        if (idx < customEmojis.size) {
                                            val emoji = customEmojis[idx]
                                            Text(
                                                text = emoji,
                                                style = androidx.compose.ui.text.TextStyle(fontSize = 32.sp),
                                                modifier = Modifier
                                                    .clickable {
                                                        quickEmoji = emoji
                                                        showEmojiCustomizerDialog = false
                                                    }
                                                    .padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Hủy",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                            .clickable { showEmojiCustomizerDialog = false }
                                            .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    partnerName: String?,
    partnerBitmap: Bitmap?,
    partnerInitial: String?,
    isTyping: Boolean,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surface,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarImage(
                        bitmap = partnerBitmap,
                        size = 38,
                        initial = partnerInitial,
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF31A24C), CircleShape)
                            .border(1.5.dp, colorScheme.surface, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = partnerName ?: stringResource(R.string.chat_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (isTyping) "Đang nhập tin nhắn..." else "Đang hoạt động",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (isTyping) Color(0xFF31A24C) else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = if (isTyping) FontWeight.Medium else FontWeight.Normal,
                    )
                }

                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Info,
                        contentDescription = "Info",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onSendLike: () -> Unit,
    quickEmoji: String,
    isSending: Boolean,
    accessToken: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isTextEmpty = value.text.isEmpty()
    val canSend = !isSending && accessToken.isNotBlank()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surface,
    ) {
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .padding(start = 4.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.chat_input_placeholder),
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    },
                    singleLine = false,
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = colorScheme.surfaceVariant,
                        unfocusedContainerColor = colorScheme.surfaceVariant,
                        cursorColor = colorScheme.primary,
                    ),
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .then(
                            if (isTextEmpty) Modifier.width(40.dp)
                            else Modifier.padding(horizontal = 8.dp)
                        )
                        .background(
                            color = Color.Transparent,
                            shape = CircleShape,
                        )
                        .clickable(
                            enabled = canSend,
                            onClick = {
                                if (isTextEmpty) {
                                    onSendLike()
                                } else {
                                    onSend()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isTextEmpty) {
                        Text(
                            text = quickEmoji,
                            fontSize = 24.sp,
                        )
                    } else {
                        Text(
                            text = "Gửi",
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

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
    positionInGroup: MessagePositionInGroup,
    allMessages: List<ChatMessage>,
    partnerBitmap: Bitmap?,
    partnerInitial: String?,
    partnerName: String?,
    lastReadMessageId: String?,
    reactionsMap: Map<String, String>,
    activeReactionMenuMessageId: String?,
    onReact: (String) -> Unit,
    onShowReactionMenu: (Boolean) -> Unit,
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
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
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
            }
        },
    ) {
        ChatBubble(
            message = message,
            showAvatar = showAvatar,
            showTimestamp = showTimestamp,
            positionInGroup = positionInGroup,
            allMessages = allMessages,
            partnerBitmap = partnerBitmap,
            partnerInitial = partnerInitial,
            partnerName = partnerName,
            lastReadMessageId = lastReadMessageId,
            reactionsMap = reactionsMap,
            showReactionMenu = activeReactionMenuMessageId == message.id,
            onReact = onReact,
            onShowReactionMenu = onShowReactionMenu,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chat bubble
// ─────────────────────────────────────────────────────────────────────────────

private fun getBubbleShape(mine: Boolean, position: MessagePositionInGroup): RoundedCornerShape {
    val defaultRadius = 18.dp
    val flatRadius = 4.dp
    return if (mine) {
        when (position) {
            MessagePositionInGroup.SINGLE -> RoundedCornerShape(defaultRadius, defaultRadius, defaultRadius, defaultRadius)
            MessagePositionInGroup.FIRST -> RoundedCornerShape(defaultRadius, defaultRadius, defaultRadius, flatRadius)
            MessagePositionInGroup.MIDDLE -> RoundedCornerShape(defaultRadius, flatRadius, defaultRadius, flatRadius)
            MessagePositionInGroup.LAST -> RoundedCornerShape(defaultRadius, flatRadius, defaultRadius, defaultRadius)
        }
    } else {
        when (position) {
            MessagePositionInGroup.SINGLE -> RoundedCornerShape(defaultRadius, defaultRadius, defaultRadius, defaultRadius)
            MessagePositionInGroup.FIRST -> RoundedCornerShape(defaultRadius, defaultRadius, flatRadius, defaultRadius)
            MessagePositionInGroup.MIDDLE -> RoundedCornerShape(flatRadius, defaultRadius, flatRadius, defaultRadius)
            MessagePositionInGroup.LAST -> RoundedCornerShape(flatRadius, defaultRadius, defaultRadius, defaultRadius)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(
    message: ChatMessage,
    showAvatar: Boolean,
    showTimestamp: Boolean,
    positionInGroup: MessagePositionInGroup,
    allMessages: List<ChatMessage>,
    partnerBitmap: Bitmap?,
    partnerInitial: String?,
    partnerName: String?,
    lastReadMessageId: String?,
    reactionsMap: Map<String, String>,
    showReactionMenu: Boolean,
    onReact: (String) -> Unit,
    onShowReactionMenu: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mine = message.mine
    val colorScheme = MaterialTheme.colorScheme

    val bubbleShape = getBubbleShape(mine, positionInGroup)
    val mineGradient = Brush.linearGradient(colors = listOf(colorScheme.primary, colorScheme.tertiary))
    val partnerColor = colorScheme.surfaceVariant

    val textColor = if (mine) Color.White else colorScheme.onSurface
    val timeColor = if (mine) Color.White.copy(alpha = 0.65f) else colorScheme.onSurfaceVariant

    val timeText = formatTimestamp(message.createdAt)

    val quotedMessage = remember(message.replyToId, allMessages) {
        message.replyToId?.let { id -> allMessages.find { it.id == id } }
    }
    val isAi = !mine && message.senderUsername?.equals("MiniAI", ignoreCase = true) == true

    val msgAvatarBitmap = when {
        mine || isAi -> null
        partnerBitmap != null -> partnerBitmap
        else -> remember(message.senderAvatarUrl) { decodeBase64Avatar(message.senderAvatarUrl) }
    }

    val reaction = reactionsMap[message.id]

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
            if (isAi && showAvatar) {
                Text(
                    text = "MiniAI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                )
            }

            if (showReactionMenu) {
                Popup(
                    alignment = if (mine) Alignment.TopEnd else Alignment.TopStart,
                    onDismissRequest = { onShowReactionMenu(false) }
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colorScheme.surfaceContainerHigh,
                        shadowElevation = 6.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "😡")
                            emojis.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .clickable {
                                            onReact(emoji)
                                            onShowReactionMenu(false)
                                        }
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (mine) Alignment.BottomEnd else Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (mine) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = bubbleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .then(
                                if (mine) Modifier.background(mineGradient, bubbleShape)
                                else Modifier.background(partnerColor, bubbleShape)
                            )
                            .combinedClickable(
                                onClick = {
                                    if (showReactionMenu) onShowReactionMenu(false)
                                },
                                onLongClick = {
                                    onShowReactionMenu(true)
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            if (quotedMessage != null) {
                                QuotedMessagePreview(
                                    quoted = quotedMessage,
                                    isOnMine = mine,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                            MessageTextWithLinks(
                                messageText = message.text,
                                textColor = textColor,
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
                                }
                            }
                        }
                    }
                }

                if (reaction != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, colorScheme.background),
                        modifier = Modifier
                            .offset(
                                x = if (mine) (-12).dp else 12.dp,
                                y = 6.dp
                            )
                    ) {
                        Text(
                            text = reaction,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            val isLatestReadMessage = message.id == lastReadMessageId
            if (mine) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 4.dp, top = 2.dp)
                ) {
                    when {
                        message.readStatus == com.example.mobileproject.domain.entity.ReadStatus.READ && isLatestReadMessage -> {
                            AvatarImage(
                                bitmap = partnerBitmap,
                                size = 14,
                                initial = partnerInitial,
                            )
                        }
                    }
                }
            }
        }

        if (mine) Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun MessageTextWithLinks(
    messageText: String,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val colorScheme = MaterialTheme.colorScheme
    val plannerCard = remember(messageText) { ExplorePlanChatCardParser.parse(messageText) }
    val annotated = remember(messageText, textColor, colorScheme.primary) {
        buildAnnotatedMessage(
            messageText = messageText,
            textColor = textColor,
            linkColor = colorScheme.primary,
        )
    }

    if (plannerCard != null) {
        ExplorePlanChatCard(
            card = plannerCard,
            onOpenMaps = {
                plannerCard.googleMapsUrl?.let(uriHandler::openUri)
            },
            modifier = modifier,
        )
        return
    }

    ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            lineHeight = 20.sp,
        ),
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = MAP_URL_TAG, start = offset, end = offset)
                .firstOrNull()
                ?.item
                ?.let(uriHandler::openUri)
        },
    )
}

@Composable
private fun ExplorePlanChatCard(
    card: ExplorePlanChatCardData,
    onOpenMaps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val density = LocalDensity.current
    val cardBodyColor = colorScheme.primaryContainer
    val cardBodyText = colorScheme.onPrimaryContainer
    val cardBodyMuted = colorScheme.onSurfaceVariant
    val ctaColor = colorScheme.primary
    val fallbackImageUrl = remember(card.title) { fallbackImageFor(card.title) }
    val primaryImageUrl = card.imageUrl?.takeIf { it.isNotBlank() }
    var imageModel by remember(card.title, primaryImageUrl) {
        mutableStateOf(primaryImageUrl ?: fallbackImageUrl)
    }
    val imageRequest = remember(imageModel, context) {
        val widthPx = with(density) { 360.dp.roundToPx() }
        val heightPx = with(density) { 196.dp.roundToPx() }
        ImageRequest.Builder(context)
            .data(imageModel)
            .size(widthPx, heightPx)
            .crossfade(false)
            .build()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBodyColor),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ctaColor.copy(alpha = 0.92f),
                                    colorScheme.tertiary.copy(alpha = 0.88f),
                                ),
                            ),
                        ),
                )
                Icon(
                    imageVector = Icons.Rounded.Restaurant,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp),
                )
                AsyncImage(
                    model = imageRequest,
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = {
                        if (imageModel != fallbackImageUrl) {
                            imageModel = fallbackImageUrl
                        }
                    },
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colorScheme.scrim.copy(alpha = 0.8f),
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.chat_plan_card_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        card.stopOrder?.let {
                            PlannerInfoPill(
                                text = "Điểm dừng $it",
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White,
                            )
                        }
                        card.experienceType?.let {
                            PlannerInfoPill(
                                text = it,
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White,
                            )
                        }
                        card.estimatedCostLabel?.let {
                            PlannerInfoPill(
                                text = it,
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                card.reason?.let {
                    PlannerCardDetail(
                        label = "Vì sao",
                        value = it,
                        textColor = cardBodyText,
                        labelColor = cardBodyMuted,
                    )
                }
                card.address?.let {
                    PlannerCardDetail(
                        label = "Địa chỉ",
                        value = it,
                        textColor = cardBodyText,
                        labelColor = cardBodyMuted,
                    )
                }
                if (!card.googleMapsUrl.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenMaps),
                        shape = RoundedCornerShape(16.dp),
                        color = ctaColor,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Place,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.chat_plan_card_open_maps),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerInfoPill(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun PlannerCardDetail(
    label: String,
    value: String,
    textColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }
}

private fun buildAnnotatedMessage(
    messageText: String,
    textColor: androidx.compose.ui.graphics.Color,
    linkColor: androidx.compose.ui.graphics.Color,
): AnnotatedString {
    val matches = MAP_URL_REGEX.findAll(messageText).toList()
    if (matches.isEmpty()) {
        return AnnotatedString(
            messageText,
            spanStyle = SpanStyle(color = textColor),
        )
    }

    return buildAnnotatedString {
        var lastIndex = 0
        matches.forEach { match ->
            if (match.range.first > lastIndex) {
                append(messageText.substring(lastIndex, match.range.first))
            }

            val url = match.value
            pushStringAnnotation(tag = MAP_URL_TAG, annotation = url)
            withStyle(
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(url)
            }
            pop()
            lastIndex = match.range.last + 1
        }

        if (lastIndex < messageText.length) {
            append(messageText.substring(lastIndex))
        }
    }
}

private fun compactMessagePreview(messageText: String): String {
    val plannerCard = ExplorePlanChatCardParser.parse(messageText) ?: return messageText
    return buildString {
        append(plannerCard.title)
        plannerCard.stopOrder?.let {
            append(" • #")
            append(it)
        }
        plannerCard.experienceType?.let {
            append(" • ")
            append(it)
        }
    }
}

private const val MAP_URL_TAG = "MAP_URL"
private val MAP_URL_REGEX = Regex("""https?://[^\s]+""")

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
                text = compactMessagePreview(quoted.text),
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
            .border(
                width = 3.dp,
                color = colorScheme.primary,
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
            )
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
                text = compactMessagePreview(message.text),
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

private fun completeMention(
    value: TextFieldValue,
    tokenStart: Int,
    tokenEnd: Int,
    mentionText: String
): TextFieldValue {
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

