package com.example.mobileproject.domain.entity

/**
 * Domain entity đại diện cho một tin nhắn trong cuộc trò chuyện giữa cặp đôi.
 * Hỗ trợ hiển thị tin nhắn gửi/nhận, trạng thái đã đọc, và reply tin nhắn cũ.
 *
 * @property id Định danh duy nhất của tin nhắn, dùng cho reply và đồng bộ
 * @property text Nội dung văn bản của tin nhắn
 * @property senderUsername Tên người gửi, null nếu là tin nhắn hệ thống
 * @property mine True nếu tin nhắn do người dùng hiện tại gửi, dùng để căn chỉnh UI (trái/phải)
 * @property createdAt Thời điểm gửi tin nhắn dạng chuỗi ISO
 * @property senderAvatarUrl URL avatar của người gửi, dùng để hiển thị trong chat bubble
 * @property readStatus Trạng thái đã đọc của tin nhắn, mặc định là SENT khi vừa gửi
 * @property replyToId Định danh tin nhắn được reply, null nếu đây là tin nhắn gốc
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val senderUsername: String?,
    val mine: Boolean,
    val createdAt: String?,
    val senderAvatarUrl: String? = null,
    val readStatus: ReadStatus = ReadStatus.SENT,
    val replyToId: String? = null,
)

/**
 * Enum đại diện cho trạng thái đọc của tin nhắn.
 * Được sử dụng để hiển thị icon trạng thái bên cạnh tin nhắn đã gửi.
 *
 * - SENT: Tin nhắn đã được gửi lên server nhưng chưa được deliver đến người nhận
 * - DELIVERED: Tin nhắn đã được deliver đến thiết bị người nhận nhưng chưa đọc
 * - READ: Người nhận đã mở và đọc tin nhắn
 */
enum class ReadStatus { SENT, DELIVERED, READ }
