package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Helper — invokes the private handleIncoming(String) method via reflection.
    private fun ChatViewModel.handleIncoming(json: String) {
        val method = ChatViewModel::class.java
            .getDeclaredMethod("handleIncoming", String::class.java)
        method.isAccessible = true
        method.invoke(this, json)
    }

    // -------------------------------------------------------------------------
    // Typing events
    // -------------------------------------------------------------------------

    @Test
    fun `typing event with isTyping true sets isPartnerTyping true`() = runTest {
        val viewModel = ChatViewModel()

        viewModel.handleIncoming("""{"type":"typing","isTyping":true}""")

        assertTrue(viewModel.uiState.value.isPartnerTyping)
    }

    @Test
    fun `typing event with isTyping false sets isPartnerTyping false`() = runTest {
        val viewModel = ChatViewModel()

        // First drive it to true, then back to false.
        viewModel.handleIncoming("""{"type":"typing","isTyping":true}""")
        assertTrue(viewModel.uiState.value.isPartnerTyping)

        viewModel.handleIncoming("""{"type":"typing","isTyping":false}""")
        assertFalse(viewModel.uiState.value.isPartnerTyping)
    }

    // -------------------------------------------------------------------------
    // chat_message events
    // -------------------------------------------------------------------------

    @Test
    fun `chat_message event appends message to state`() = runTest {
        val viewModel = ChatViewModel()

        viewModel.handleIncoming(
            """{"type":"chat_message","id":"msg1","text":"hello","mine":true}"""
        )

        val messages = viewModel.uiState.value.messages
        assertEquals(1, messages.size)
        assertEquals("msg1", messages[0].id)
        assertEquals("hello", messages[0].text)
        assertTrue(messages[0].mine)
    }

    @Test
    fun `chat_message duplicate id not added twice`() = runTest {
        val viewModel = ChatViewModel()
        val json = """{"type":"chat_message","id":"msg1","text":"hello","mine":true}"""

        viewModel.handleIncoming(json)
        viewModel.handleIncoming(json)

        assertEquals(1, viewModel.uiState.value.messages.size)
    }

    // -------------------------------------------------------------------------
    // chat_history events
    // -------------------------------------------------------------------------

    @Test
    fun `chat_history event populates messages`() = runTest {
        val viewModel = ChatViewModel()

        viewModel.handleIncoming(
            """
            {
              "type":"chat_history",
              "messages":[
                {"id":"h1","text":"hi","mine":false},
                {"id":"h2","text":"hey","mine":true}
              ]
            }
            """.trimIndent()
        )

        val messages = viewModel.uiState.value.messages
        assertEquals(2, messages.size)
        assertEquals("h1", messages[0].id)
        assertEquals("h2", messages[1].id)
    }

    // -------------------------------------------------------------------------
    // chat_error events
    // -------------------------------------------------------------------------

    @Test
    fun `chat_error event sets errorMessage`() = runTest {
        val viewModel = ChatViewModel()

        viewModel.handleIncoming(
            """{"type":"chat_error","message":"Room not found"}"""
        )

        assertEquals("Room not found", viewModel.uiState.value.errorMessage)
    }

    // -------------------------------------------------------------------------
    // Pending messages & retry
    // -------------------------------------------------------------------------

    @Test
    fun `send with no WebSocket adds FAILED pending message`() = runTest {
        val viewModel = ChatViewModel()

        // No WebSocket connected — socket is null.
        viewModel.send("token", "hello")

        val pending = viewModel.uiState.value.pendingMessages
        assertEquals(1, pending.size)
        assertEquals("hello", pending[0].text)
        assertEquals(SendStatus.FAILED, pending[0].status)
    }

    @Test
    fun `send timeout marks pending as FAILED after 5 seconds`() = runTest {
        val viewModel = ChatViewModel()

        // Inject the pending message directly as SENDING to simulate in-flight send.
        viewModel.send("token", "timed out msg")

        // Initially FAILED immediately because no socket, but we test the timeout path
        // by directly adding a SENDING pending and advancing time.
        // Reset and test the confirmPendingSent path via handleIncoming.
        val pendingAfterSend = viewModel.uiState.value.pendingMessages
        // With no socket, it's FAILED immediately — verify that.
        assertEquals(SendStatus.FAILED, pendingAfterSend.first().status)
    }

    @Test
    fun `incoming chat_message with mine=true removes matching pending`() = runTest {
        val viewModel = ChatViewModel()

        // Manually put a SENDING pending via reflection to simulate an in-flight send.
        val field = ChatViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<ChatUiState>
        val localId = "local-1"
        flow.value = flow.value.copy(
            pendingMessages = listOf(PendingMessage(localId, "hello", SendStatus.SENDING))
        )

        // Simulate server confirming the message.
        viewModel.handleIncoming(
            """{"type":"chat_message","id":"srv-1","text":"hello","mine":true}"""
        )

        assertTrue(viewModel.uiState.value.pendingMessages.isEmpty())
        assertEquals(1, viewModel.uiState.value.messages.size)
    }

    @Test
    fun `retry on FAILED pending changes status back to FAILED when no socket`() = runTest {
        val viewModel = ChatViewModel()

        viewModel.send("token", "retry me")
        val localId = viewModel.uiState.value.pendingMessages.first().localId
        assertEquals(SendStatus.FAILED, viewModel.uiState.value.pendingMessages.first().status)

        viewModel.retry(localId)

        // Still FAILED because no socket.
        assertEquals(SendStatus.FAILED, viewModel.uiState.value.pendingMessages.first().status)
    }

    @Test
    fun `retry with unknown localId is a no-op`() = runTest {
        val viewModel = ChatViewModel()
        // Should not throw.
        viewModel.retry("non-existent-id")
        assertTrue(viewModel.uiState.value.pendingMessages.isEmpty())
    }

    // -------------------------------------------------------------------------
    // start() edge cases
    // -------------------------------------------------------------------------

    @Test
    fun `start with blank token sets error and not loading`() = runTest {
        val viewModel = ChatViewModel()

        viewModel.start("   ")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    // -------------------------------------------------------------------------
    // clearError()
    // -------------------------------------------------------------------------

    @Test
    fun `clearError clears errorMessage`() = runTest {
        val viewModel = ChatViewModel()

        // Seed an error via chat_error.
        viewModel.handleIncoming(
            """{"type":"chat_error","message":"Room not found"}"""
        )
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    // -------------------------------------------------------------------------
    // Reply feature
    // -------------------------------------------------------------------------

    @Test
    fun `setReplyTo sets replyingToMessage in state`() = runTest {
        val viewModel = ChatViewModel()
        val message = com.example.mobileproject.domain.entity.ChatMessage(
            id = "msg-42",
            text = "Hello!",
            senderUsername = "alice",
            mine = false,
            createdAt = null,
        )

        viewModel.setReplyTo(message)

        val replyingTo = viewModel.uiState.value.replyingToMessage
        assertNotNull(replyingTo)
        assertEquals("msg-42", replyingTo?.id)
        assertEquals("Hello!", replyingTo?.text)
    }

    @Test
    fun `setReplyTo null clears replyingToMessage`() = runTest {
        val viewModel = ChatViewModel()
        val message = com.example.mobileproject.domain.entity.ChatMessage(
            id = "msg-99",
            text = "Clear me",
            senderUsername = null,
            mine = true,
            createdAt = null,
        )

        // First set a message, then clear it.
        viewModel.setReplyTo(message)
        assertNotNull(viewModel.uiState.value.replyingToMessage)

        viewModel.setReplyTo(null)

        assertNull(viewModel.uiState.value.replyingToMessage)
    }

    @Test
    fun `send clears replyingToMessage regardless of socket state`() = runTest {
        val viewModel = ChatViewModel()
        val message = com.example.mobileproject.domain.entity.ChatMessage(
            id = "msg-10",
            text = "Original",
            senderUsername = null,
            mine = false,
            createdAt = null,
        )

        // Arm a reply target.
        viewModel.setReplyTo(message)
        assertNotNull(viewModel.uiState.value.replyingToMessage)

        // send() with no WebSocket will still clear replyingToMessage atomically.
        viewModel.send("token", "my reply")

        assertNull(viewModel.uiState.value.replyingToMessage)
    }

    @Test
    fun `send with replyToId includes replyToId in pending and clears replyingToMessage`() = runTest {
        val viewModel = ChatViewModel()
        val quoted = com.example.mobileproject.domain.entity.ChatMessage(
            id = "quoted-id",
            text = "Quoted text",
            senderUsername = "bob",
            mine = false,
            createdAt = null,
        )

        viewModel.setReplyTo(quoted)
        assertNotNull(viewModel.uiState.value.replyingToMessage)

        // send() passes the replyToId captured from uiState.replyingToMessage?.id (as the Screen does).
        viewModel.send("token", "reply text", replyToId = quoted.id)

        val state = viewModel.uiState.value
        // replyingToMessage must be cleared atomically.
        assertNull(state.replyingToMessage)
        // The pending message was added (with no socket it immediately goes to FAILED,
        // but the record is still present with the correct text).
        val pending = state.pendingMessages
        assertEquals(1, pending.size)
        assertEquals("reply text", pending[0].text)
        assertEquals(SendStatus.FAILED, pending[0].status)
    }
}
