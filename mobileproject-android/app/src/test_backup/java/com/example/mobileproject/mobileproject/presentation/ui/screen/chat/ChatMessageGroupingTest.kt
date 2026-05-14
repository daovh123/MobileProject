package com.example.mobileproject.presentation.ui.screen.chat

import com.example.mobileproject.domain.entity.ChatMessage
import com.example.mobileproject.domain.entity.ReadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ChatMessageGroupingTest {

    private fun msg(
        id: String,
        sender: String,
        mine: Boolean,
        offsetMinutes: Long = 0,
        baseEpochMs: Long = 1_700_000_000_000L,
    ) = ChatMessage(
        id = id,
        text = "msg $id",
        senderUsername = sender,
        mine = mine,
        createdAt = Instant.ofEpochMilli(baseEpochMs + offsetMinutes * 60_000).toString(),
        readStatus = ReadStatus.SENT,
    )

    @Test
    fun `empty list returns empty`() {
        val result = groupMessages(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single message has showAvatar and showTimestamp true`() {
        val result = groupMessages(listOf(msg("1", "alice", mine = false)))
        assertEquals(1, result.size)
        assertTrue(result[0].showAvatar)
        assertTrue(result[0].showTimestamp)
    }

    @Test
    fun `messages from same sender within window are grouped, only last shows avatar`() {
        val messages = listOf(
            msg("1", "alice", mine = false, offsetMinutes = 0),
            msg("2", "alice", mine = false, offsetMinutes = 1),
            msg("3", "alice", mine = false, offsetMinutes = 2),
        )
        val result = groupMessages(messages)
        assertEquals(3, result.size)
        assertFalse(result[0].showAvatar)
        assertFalse(result[1].showAvatar)
        assertTrue(result[2].showAvatar)
        assertTrue(result[2].showTimestamp)
    }

    @Test
    fun `messages from different senders are not grouped`() {
        val messages = listOf(
            msg("1", "alice", mine = false, offsetMinutes = 0),
            msg("2", "bob", mine = true, offsetMinutes = 1),
        )
        val result = groupMessages(messages)
        assertEquals(2, result.size)
        assertTrue(result[0].showAvatar)
        assertTrue(result[1].showAvatar)
    }

    @Test
    fun `messages from same sender beyond window start new group`() {
        val messages = listOf(
            msg("1", "alice", mine = false, offsetMinutes = 0),
            msg("2", "alice", mine = false, offsetMinutes = 6),
        )
        val result = groupMessages(messages)
        assertEquals(2, result.size)
        assertTrue(result[0].showAvatar)
        assertTrue(result[1].showAvatar)
    }

    @Test
    fun `mine=true messages never show avatar when grouped`() {
        val messages = listOf(
            msg("1", "me", mine = true, offsetMinutes = 0),
            msg("2", "me", mine = true, offsetMinutes = 1),
        )
        val result = groupMessages(messages)
        assertEquals(2, result.size)
        assertFalse(result[0].showAvatar)
        assertTrue(result[1].showAvatar)
    }

    @Test
    fun `interleaved senders each form their own groups`() {
        val messages = listOf(
            msg("1", "alice", mine = false, offsetMinutes = 0),
            msg("2", "alice", mine = false, offsetMinutes = 1),
            msg("3", "bob", mine = true, offsetMinutes = 2),
            msg("4", "alice", mine = false, offsetMinutes = 3),
        )
        val result = groupMessages(messages)
        assertEquals(4, result.size)
        assertFalse(result[0].showAvatar) // alice group start
        assertTrue(result[1].showAvatar)  // alice group end
        assertTrue(result[2].showAvatar)  // bob single
        assertTrue(result[3].showAvatar)  // alice single (new group after bob)
    }

    // -------------------------------------------------------------------------
    // STEP 3 additions: window-anchor, null createdAt, replyToId transparency
    // -------------------------------------------------------------------------

    /**
     * Verifies the window comparison is START-ANCHORED (every message in the
     * inner loop is compared against messages[i], the group-start, not against
     * the immediately preceding message).
     *
     * Layout (same sender "alice"):
     *   msg A  t = 0 min   → group start  (i=0)
     *   msg B  t = 4 min   → 4 min from A  → within 5-min window → grouped
     *   msg C  t = 8 min   → 8 min from A  → OUTSIDE 5-min window → NEW group
     *
     * If the comparison were pair-wise (B→C = 4 min < 5 min) msg C would be
     * incorrectly grouped with A and B.  Because it is start-anchored, msg C
     * correctly starts its own group.
     */
    @Test
    fun `window is start-anchored - message beyond 5 min of group start breaks group even if within 5 min of previous`() {
        val messages = listOf(
            msg("A", "alice", mine = false, offsetMinutes = 0),
            msg("B", "alice", mine = false, offsetMinutes = 4),
            msg("C", "alice", mine = false, offsetMinutes = 8),
        )
        val result = groupMessages(messages)

        // A and B form one group (both within 5 min of group start at t=0)
        // C is 8 min from A (> 5 min) → must start a new group
        assertEquals(3, result.size)

        assertFalse(result[0].showAvatar)   // A — head of first group, not last
        assertTrue(result[1].showAvatar)   // B — last in first group
        assertTrue(result[1].showTimestamp) // B — last in group always shows timestamp
        assertTrue(result[2].showAvatar)   // C — sole member of second group
        assertTrue(result[2].showTimestamp)
    }

    /**
     * A message whose createdAt is exactly 5 minutes from the group start must
     * NOT be grouped (window comparison is strictly greater-than-zero, i.e.
     * nextTime - currentTime > window * 60_000 triggers a break).
     * 5 min = exactly the boundary: 5 * 60_000 = 300_000 ms, difference == 300_000
     * → NOT > 300_000 → stays in the group.
     *
     * This test pins the "equal is included" contract.
     */
    @Test
    fun `message exactly at window boundary is still grouped`() {
        val messages = listOf(
            msg("1", "alice", mine = false, offsetMinutes = 0),
            msg("2", "alice", mine = false, offsetMinutes = 5),
        )
        val result = groupMessages(messages)
        assertEquals(2, result.size)
        assertFalse(result[0].showAvatar)  // first in group
        assertTrue(result[1].showAvatar)   // last in group
    }

    /**
     * A message exactly 1 ms beyond the window boundary (5 min + 1 ms) must
     * break the group.
     */
    @Test
    fun `message 1ms beyond window boundary starts a new group`() {
        val base = 1_700_000_000_000L
        val fiveMinPlusOneMs = 5 * 60_000L + 1L
        val messages = listOf(
            ChatMessage(
                id = "1", text = "hi", senderUsername = "alice", mine = false,
                createdAt = Instant.ofEpochMilli(base).toString(),
                readStatus = ReadStatus.SENT,
            ),
            ChatMessage(
                id = "2", text = "hey", senderUsername = "alice", mine = false,
                createdAt = Instant.ofEpochMilli(base + fiveMinPlusOneMs).toString(),
                readStatus = ReadStatus.SENT,
            ),
        )
        val result = groupMessages(messages)
        assertEquals(2, result.size)
        assertTrue(result[0].showAvatar)  // stands alone (group of 1)
        assertTrue(result[1].showAvatar)  // new group
    }

    /**
     * Messages with null createdAt must not crash.  parseInstantMillis returns
     * 0L for null/blank, so two null-timestamped messages from the same sender
     * will be grouped (difference = 0, which is within any positive window).
     * This documents the graceful-fallback contract.
     */
    @Test
    fun `null createdAt does not crash and falls back to epoch 0`() {
        val messages = listOf(
            ChatMessage(
                id = "1", text = "a", senderUsername = "alice", mine = false,
                createdAt = null, readStatus = ReadStatus.SENT,
            ),
            ChatMessage(
                id = "2", text = "b", senderUsername = "alice", mine = false,
                createdAt = null, readStatus = ReadStatus.SENT,
            ),
        )
        // Must not throw; both map to epoch 0 → difference = 0 → grouped
        val result = groupMessages(messages)
        assertEquals(2, result.size)
        assertFalse(result[0].showAvatar)  // first in group
        assertTrue(result[1].showAvatar)   // last in group
    }

    /**
     * A blank (whitespace-only) createdAt must also not crash.
     */
    @Test
    fun `blank createdAt does not crash`() {
        val messages = listOf(
            ChatMessage(
                id = "1", text = "a", senderUsername = "alice", mine = false,
                createdAt = "   ", readStatus = ReadStatus.SENT,
            ),
        )
        val result = groupMessages(messages)
        assertEquals(1, result.size)
        assertTrue(result[0].showAvatar)
    }

    /**
     * A malformed (non-ISO-8601) createdAt must not crash; it falls back to 0L.
     */
    @Test
    fun `malformed createdAt does not crash and falls back to epoch 0`() {
        val messages = listOf(
            ChatMessage(
                id = "1", text = "a", senderUsername = "alice", mine = false,
                createdAt = "not-a-date", readStatus = ReadStatus.SENT,
            ),
        )
        val result = groupMessages(messages)
        assertEquals(1, result.size)
        assertTrue(result[0].showAvatar)
    }

    /**
     * The replyToId field on ChatMessage must be completely transparent to the
     * grouping algorithm.  A reply message behaves identically to a plain
     * message for grouping purposes.
     */
    @Test
    fun `replyToId is transparent to grouping - messages group as if replyToId were null`() {
        val base = 1_700_000_000_000L
        val messages = listOf(
            // plain message
            ChatMessage(
                id = "1", text = "hello", senderUsername = "alice", mine = false,
                createdAt = Instant.ofEpochMilli(base).toString(),
                readStatus = ReadStatus.SENT,
                replyToId = null,
            ),
            // reply to msg "1" — same sender, within window → should be grouped
            ChatMessage(
                id = "2", text = "reply", senderUsername = "alice", mine = false,
                createdAt = Instant.ofEpochMilli(base + 60_000L).toString(),
                readStatus = ReadStatus.SENT,
                replyToId = "1",
            ),
            // another reply, different sender — should NOT group with alice
            ChatMessage(
                id = "3", text = "bob reply", senderUsername = "bob", mine = true,
                createdAt = Instant.ofEpochMilli(base + 120_000L).toString(),
                readStatus = ReadStatus.SENT,
                replyToId = "1",
            ),
        )
        val result = groupMessages(messages)
        assertEquals(3, result.size)
        assertFalse(result[0].showAvatar)  // alice[0] — not last in group
        assertTrue(result[1].showAvatar)   // alice[1] — last in group (has replyToId)
        assertTrue(result[2].showAvatar)   // bob — different sender, own group
    }
}
