/**
 * Message grouping algorithm – gom nhóm tin nhắn liên tiếp từ cùng một người gửi
 * để hiển thị avatar và timestamp chỉ ở đầu/cuối nhóm, tạo giao diện chat gọn gàng.
 *
 * Thuật toán:
 * 1. Duyệt danh sách tin nhắn theo thứ tự thời gian.
 * 2. Gom nhóm các tin liên tiếp nếu thỏa mãn ĐIỀU KIỆN:
 *    - Cùng người gửi (same [ChatMessage.mine] + same senderUsername).
 *    - Khoảng thời gian giữa 2 tin ≤ [groupWindowMinutes] (mặc định 5 phút).
 * 3. Với mỗi nhóm:
 *    - Nhóm 1 tin: position = [MessagePositionInGroup.SINGLE], hiện avatar + timestamp.
 *    - Nhóm ≥2 tin: FIRST (ẩn avatar, ẩn timestamp), MIDDLE (ẩn cả hai),
 *      LAST (hiện avatar + timestamp).
 *
 * Kết quả trả về: List<GroupedMessage> – mỗi phần tử chứa [ChatMessage] gốc
 * cùng metadata hiển thị (showAvatar, showTimestamp, positionInGroup).
 */
package com.example.mobileproject.presentation.ui.screen.chat

import com.example.mobileproject.domain.entity.ChatMessage

/**
 * Vị trí của tin nhắn trong một nhóm liên tiếp.
 * Dùng để xác định bo góc bong bóng chat và hiển thị avatar/timestamp.
 */
enum class MessagePositionInGroup {
    SINGLE, FIRST, MIDDLE, LAST
}

/**
 * Đại diện cho một tin nhắn đã được gom nhóm, chứa metadata hiển thị.
 *
 * @property message tin nhắn gốc từ domain layer.
 * @property showAvatar có hiển thị avatar của người gửi hay không.
 * @property showTimestamp có hiển thị thời gian gửi hay không.
 * @property positionInGroup vị trí trong nhóm: SINGLE/FIRST/MIDDLE/LAST.
 */
internal data class GroupedMessage(
    val message: ChatMessage,
    val showAvatar: Boolean,
    val showTimestamp: Boolean,
    val positionInGroup: MessagePositionInGroup = MessagePositionInGroup.SINGLE,
)

/**
 * Gom nhóm tin nhắn liên tiếp từ cùng người gửi trong cửa sổ thời gian [groupWindowMinutes].
 *
 * Message grouping algorithm:
 * - Duyệt tuyến tính O(n), dùng two-pointer (i, j) để xác định range nhóm.
 * - Điều kiện ngắt nhóm: khác người gửi HOẶC chênh lệch thời gian > 5 phút.
 * - Tin nhắn đầu nhóm: ẩn avatar (vì trùng với cuối nhóm trước), ẩn timestamp.
 * - Tin nhắn cuối nhóm: hiện avatar + timestamp (tránh lặp lại liên tục).
 */
internal fun groupMessages(
    messages: List<ChatMessage>,
    groupWindowMinutes: Long = 5,
): List<GroupedMessage> {
    if (messages.isEmpty()) return emptyList()
    val result = mutableListOf<GroupedMessage>()
    var i = 0
    while (i < messages.size) {
        val current = messages[i]
        var j = i
        // Duyệt forward để tìm điểm ngắt nhóm: khác sender hoặc chênh lệch thời gian > groupWindowMinutes
        while (j + 1 < messages.size) {
            val next = messages[j + 1]
            if (next.mine != current.mine || next.senderUsername != current.senderUsername) break
            val currentTime = parseInstantMillis(current.createdAt)
            val nextTime = parseInstantMillis(next.createdAt)
            if (nextTime - currentTime > groupWindowMinutes * 60_000) break
            j++
        }
        if (i == j) {
            result.add(
                GroupedMessage(
                    message = messages[i],
                    showAvatar = true,
                    showTimestamp = true,
                    positionInGroup = MessagePositionInGroup.SINGLE
                )
            )
        } else {
            result.add(
                GroupedMessage(
                    message = messages[i],
                    showAvatar = false,
                    showTimestamp = false,
                    positionInGroup = MessagePositionInGroup.FIRST
                )
            )
            for (k in (i + 1) until j) {
                result.add(
                    GroupedMessage(
                        message = messages[k],
                        showAvatar = false,
                        showTimestamp = false,
                        positionInGroup = MessagePositionInGroup.MIDDLE
                    )
                )
            }
            result.add(
                GroupedMessage(
                    message = messages[j],
                    showAvatar = true,
                    showTimestamp = true,
                    positionInGroup = MessagePositionInGroup.LAST
                )
            )
        }
        i = j + 1
    }
    return result
}

/**
 * Parse chuỗi ISO-8601 timestamp thành epoch millis.
 * Trả về 0L nếu parse thất bại (invalid format hoặc null).
 */
internal fun parseInstantMillis(createdAt: String?): Long {
    if (createdAt.isNullOrBlank()) return 0L
    return try {
        java.time.Instant.parse(createdAt.trim()).toEpochMilli()
    } catch (e: Exception) {
        0L
    }
}
