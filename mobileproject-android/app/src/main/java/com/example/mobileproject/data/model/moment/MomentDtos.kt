package com.example.mobileproject.data.model.moment

/**
 * DTO đại diện cho một khoảnh khắc (moment) của couple.
 *
 * Moment là bài đăng chia sẻ hình ảnh giữa hai thành viên couple.
 * Hỗ trợ reaction và comment.
 */
data class MomentDto(
    /** UUID của moment, nullable vì có thể chưa được server trả về. */
    val id: String?,
    /** UUID của couple sở hữu moment, nullable. */
    val coupleId: String?,
    /** Tiêu đề/nội dung mô tả moment, bắt buộc. */
    val title: String,
    /** URL hình ảnh của moment, bắt buộc. */
    val imageUrl: String,
    /** Thời điểm tạo, định dạng ISO datetime, nullable. */
    val createdAt: String?,
    /** Tổng số lượng reaction, mặc định 0. */
    val reactionsCount: Int = 0,
    /** Tổng số lượng bình luận, mặc định 0. */
    val commentsCount: Int = 0,
    /** Loại reaction của người xem hiện tại (VD: "LIKE", "LOVE"), null nếu chưa react. */
    val viewerReaction: String? = null,
    /** Tóm tắt các loại reaction và số lượng, mặc định rỗng. */
    val reactions: List<MomentReactionSummaryDto> = emptyList(),
)

/**
 * DTO tóm tắt một loại reaction và số lượng.
 *
 * Dùng để hiển thị danh sách reaction trên moment (VD: 👍 3, ❤️ 5).
 */
data class MomentReactionSummaryDto(
    /** Loại reaction (VD: "LIKE", "LOVE", "HAHA"). */
    val reaction: String,
    /** Số lượng người dùng đã react loại này. */
    val count: Int,
)

/**
 * Request body cho API tạo moment mới.
 *
 * Hình ảnh được gửi dưới dạng base64 string.
 */
data class MomentRequestDto(
    /** UUID của couple, bắt buộc. */
    val coupleId: String,
    /** Tiêu đề/nội dung mô tả moment, bắt buộc. */
    val title: String,
    /** Dữ liệu hình ảnh mã hóa base64, bắt buộc. */
    val base64Image: String,
)

/**
 * Response từ API tạo moment.
 */
data class MomentResponseDto(
    /** true nếu tạo moment thành công. */
    val success: Boolean,
    /** Thông báo mô tả kết quả. */
    val message: String,
    /** Moment vừa tạo, null nếu thất bại. */
    val moment: MomentDto?,
)

/**
 * Request body cho API react moment.
 *
 * Truyền [reaction] là loại reaction mong muốn.
 * Gọi lại với cùng [reaction] sẽ bỏ react (toggle behavior).
 */
data class MomentReactionRequestDto(
    /** Loại reaction (VD: "LIKE", "LOVE", "HAHA"), bắt buộc. */
    val reaction: String,
)

/**
 * Response từ API react moment.
 *
 * Trả về trạng thái reaction cập nhật sau khi react/unreact.
 */
data class MomentReactionResponseDto(
    /** UUID của moment đã react. */
    val momentId: String,
    /** Loại reaction hiện tại của người xem, null nếu đã bỏ react. */
    val viewerReaction: String?,
    /** Tổng số lượng reaction sau cập nhật. */
    val reactionsCount: Int,
    /** Tóm tắt danh sách reaction cập nhật, mặc định rỗng. */
    val reactions: List<MomentReactionSummaryDto> = emptyList(),
)

/**
 * Request body cho API bình luận moment.
 */
data class MomentCommentRequestDto(
    /** Nội dung bình luận, bắt buộc, không được rỗng. */
    val content: String,
)

/**
 * DTO đại diện cho một bình luận trên moment.
 */
data class MomentCommentDto(
    /** UUID của bình luận, nullable. */
    val id: String?,
    /** UUID của moment chứa bình luận, bắt buộc. */
    val momentId: String,
    /** Username của người bình luận, bắt buộc. */
    val authorUsername: String,
    /** Nội dung bình luận, bắt buộc. */
    val content: String,
    /** Thời điểm tạo bình luận, định dạng ISO datetime, nullable. */
    val createdAt: String?,
)
