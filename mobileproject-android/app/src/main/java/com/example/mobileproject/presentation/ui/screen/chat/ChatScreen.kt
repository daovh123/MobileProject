package com.example.mobileproject.presentation.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.ChatMessage
import com.example.mobileproject.presentation.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    accessToken: String,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var inputValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    LaunchedEffect(accessToken) {
        viewModel.start(accessToken)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(uiState.messages.size) {
        val lastIndex = uiState.messages.lastIndex
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.md3_surface_variant))
            .padding(16.dp),
    ) {
        val messages = uiState.messages
        if (messages.isEmpty() && !uiState.isLoading) {
            Text(
                text = stringResource(R.string.chat_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.md3_on_surface_variant),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 40.dp),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }
        }

        val errorMessage = uiState.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.auth_pink),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

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
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(R.string.chat_input_placeholder),
                        color = colorResource(R.color.md3_on_surface_variant),
                    )
                },
                singleLine = true,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    val toSend = inputValue.text.trim()
                    if (toSend.isNotBlank()) {
                        viewModel.send(accessToken, toSend)
                        inputValue = TextFieldValue("")
                    }
                },
                enabled = !uiState.isSending && accessToken.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.md3_primary)),
                modifier = Modifier
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Text(
                    text = stringResource(R.string.chat_send),
                    color = colorResource(R.color.md3_on_primary),
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val mine = message.mine
    val bubbleColor = if (mine) {
        colorResource(R.color.md3_primary_container)
    } else {
        colorResource(R.color.md3_surface)
    }
    val textColor = if (mine) {
        colorResource(R.color.md3_on_primary_container)
    } else {
        colorResource(R.color.md3_on_surface)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                val isAi = !mine && message.senderUsername?.equals("MiniAI", ignoreCase = true) == true
                if (isAi) {
                    Text(
                        text = "MiniAI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.md3_primary),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                )
            }
        }
    }
}

private data class MentionContext(
    val start: Int,
    val end: Int,
    val query: String,
)

private fun findMentionContext(value: TextFieldValue): MentionContext? {
    val text = value.text
    if (text.isBlank()) {
        return null
    }

    val cursor = value.selection.start.coerceIn(0, text.length)
    val start = run {
        var i = cursor - 1
        while (i >= 0) {
            val ch = text[i]
            if (ch.isWhitespace()) {
                return@run i + 1
            }
            i--
        }
        0
    }

    if (start !in 0..cursor) {
        return null
    }

    val token = text.substring(start, cursor)
    if (!token.startsWith("@")) {
        return null
    }

    val query = token.drop(1)
    return MentionContext(start = start, end = cursor, query = query)
}

private fun shouldSuggestMiniAi(query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) {
        return true
    }
    return "miniai".startsWith(q)
}

private fun completeMention(
    value: TextFieldValue,
    tokenStart: Int,
    tokenEnd: Int,
    mentionText: String,
): TextFieldValue {
    val text = value.text
    val safeStart = tokenStart.coerceIn(0, text.length)
    val safeEnd = tokenEnd.coerceIn(safeStart, text.length)

    val newText = buildString(text.length + mentionText.length + 1) {
        append(text.substring(0, safeStart))
        append(mentionText)
        append(text.substring(safeEnd))
    }
    val newCursor = (safeStart + mentionText.length).coerceIn(0, newText.length)
    return value.copy(text = newText, selection = TextRange(newCursor))
}

@Composable
private fun MiniAiSuggestionRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
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
                color = colorResource(R.color.md3_primary),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nhấn để chèn @MiniAI",
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.md3_on_surface_variant),
            )
        }
    }
}
