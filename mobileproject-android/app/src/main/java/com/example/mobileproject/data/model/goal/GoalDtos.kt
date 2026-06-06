package com.example.mobileproject.data.model.goal

import com.google.gson.annotations.SerializedName

/**
 * DTO đa năng đại diện cho mục tiêu (goal) từ API.
 *
 * Phân biệt 2 loại mục tiêu qua trường [type]:
 * - `"SAVING"`: Mục tiêu tiết kiệm — sử dụng [targetAmount], [currentAmount]
 * - `"FUTURE"`: Mục tiêu tương lai — sử dụng [progress], [tasks]
 *
 * Trường [id] và [goalId] đều có thể chứa UUID của goal,
 * mapper sẽ ưu tiên [id], fallback về [goalId].
 *
 * Trường [success] và [message] chỉ có ý nghĩa trong response của một số API (VD: tạo goal).
 */
data class GoalDto(
    /** true nếu API thao tác thành công, nullable vì không phải response nào cũng có. */
    @SerializedName("success") val success: Boolean? = null,
    /** Thông báo mô tả kết quả từ API, nullable. */
    @SerializedName("message") val message: String? = null,
    /** UUID của mục tiêu, ưu tiên dùng trường này, nullable. */
    @SerializedName("id") val id: String?,
    /** UUID mục tiêu (alternate name từ một số API), nullable. */
    @SerializedName("goalId") val goalId: String?,
    /** UUID của couple sở hữu mục tiêu này, nullable. */
    @SerializedName("coupleId") val coupleId: String?,
    /** Tên mục tiêu do người dùng đặt, nullable. */
    @SerializedName("name") val name: String?,
    /** Phân loại mục tiêu (VD: "Travel", "Shopping", "Others"), nullable. */
    @SerializedName("category") val category: String?,
    /** Loại mục tiêu: "SAVING" hoặc "FUTURE", nullable. */
    @SerializedName("type") val type: String?,
    /** Trạng thái mục tiêu (VD: "ACTIVE", "COMPLETED"), nullable. */
    @SerializedName("status") val status: String?,
    /** Hạn hoàn thành, định dạng ISO date string, nullable. */
    @SerializedName("deadline") val deadline: String?,
    /** Thời điểm tạo, định dạng ISO datetime string, nullable. */
    @SerializedName("createdAt") val createdAt: String?,
    // Saving Goal fields
    /** Số tiền mục tiêu (VNĐ), chỉ có ý nghĩa với type="SAVING", nullable. */
    @SerializedName("targetAmount") val targetAmount: Long?,
    /** Số tiền đã tiết kiệm (VNĐ), chỉ có ý nghĩa với type="SAVING", nullable. */
    @SerializedName("currentAmount") val currentAmount: Long?,
    // Future Goal fields
    /** Tiến độ hoàn thành (0.0-1.0), chỉ có ý nghĩa với type="FUTURE", nullable. */
    @SerializedName("progress") val progress: Double?,
    /** Danh sách công việc, chỉ có ý nghĩa với type="FUTURE", nullable. */
    @SerializedName("tasks") val tasks: List<GoalTaskDto>?
)

/**
 * DTO đại diện cho một công việc (task) trong mục tiêu tương lai.
 *
 * Thuộc về [GoalDto] khi type="FUTURE".
 * Trường [content] có thể null từ server, mapper sẽ chuyển thành chuỗi rỗng.
 */
data class GoalTaskDto(
    /** UUID của task, nullable. */
    @SerializedName("taskId") val taskId: String?,
    /** Nội dung mô tả task, nullable — chấp nhận null từ server và xử lý ở mapper. */
    @SerializedName("content") val content: String?,
    /** true nếu task đã hoàn thành, mặc định false. */
    @SerializedName("completed") val isCompleted: Boolean = false
)

/**
 * Request body cho API tạo mục tiêu mới.
 *
 * [coupleId], [name], [category], [type], [deadline] là bắt buộc.
 * [targetAmount] chỉ bắt buộc khi [type] = "SAVING".
 * [tasks] chỉ có ý nghĩa khi [type] = "FUTURE".
 */
data class CreateGoalRequestDto(
    /** UUID của couple, bắt buộc. */
    @SerializedName("coupleId") val coupleId: String,
    /** Tên mục tiêu, bắt buộc. */
    @SerializedName("name") val name: String,
    /** Phân loại mục tiêu, bắt buộc. */
    @SerializedName("category") val category: String,
    /** Loại mục tiêu: "SAVING" hoặc "FUTURE", bắt buộc. */
    @SerializedName("type") val type: String,
    /** Số tiền mục tiêu (VNĐ), chỉ bắt buộc khi type="SAVING", nullable. */
    @SerializedName("targetAmount") val targetAmount: Long? = null,
    /** Hạn hoàn thành, định dạng ISO date string, bắt buộc. */
    @SerializedName("deadline") val deadline: String,
    /** Danh sách công việc, chỉ có ý nghĩa khi type="FUTURE", nullable. */
    @SerializedName("tasks") val tasks: List<GoalTaskDto>? = null
)

/**
 * Response từ API đánh dấu hoàn thành/bỏ hoàn thành task.
 *
 * Trả về [progress] cập nhật sau khi toggle.
 */
data class ToggleTaskResponseDto(
    /** true nếu thao tác thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả, nullable. */
    @SerializedName("message") val message: String?,
    /** UUID của mục tiêu chứa task, nullable. */
    @SerializedName("goalId") val goalId: String?,
    /** UUID của task đã toggle, nullable. */
    @SerializedName("taskId") val taskId: String?,
    /** Tiến độ mục tiêu cập nhật (0.0-1.0), nullable. */
    @SerializedName("progress") val progress: Double?
)

/**
 * Request body cho API đóng góp vào mục tiêu từ ví chung.
 *
 * Số tiền sẽ bị trừ trực tiếp từ số dư ví couple.
 */
data class ContributeFromWalletRequestDto(
    /** Số tiền đóng góp (VNĐ), bắt buộc, phải > 0. */
    @SerializedName("amount") val amount: Long,
    /** Ghi chú cho giao dịch, nullable. */
    @SerializedName("note") val note: String?
)

/**
 * Request body cho API đóng góp trực tiếp vào mục tiêu (không qua ví).
 *
 * Dùng khi người dùng đóng góp bằng tiền mặt/ngoại tệ không qua hệ thống ví.
 */
data class ContributeDirectRequestDto(
    /** Số tiền đóng góp (VNĐ), bắt buộc, phải > 0. */
    @SerializedName("amount") val amount: Long,
    /** UUID của người đóng góp, bắt buộc. */
    @SerializedName("contributorId") val contributorId: String,
    /** Ghi chú cho giao dịch, nullable. */
    @SerializedName("note") val note: String?
)

/**
 * Response từ API đóng góp vào mục tiêu.
 *
 * Trả về thông tin cập nhật sau khi đóng góp, bao gồm số dư ví hiện tại
 * (chỉ có giá trị khi đóng góp từ ví).
 */
data class ContributionResponseDto(
    /** true nếu đóng góp thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả, nullable. */
    @SerializedName("message") val message: String?,
    /** UUID của giao dịch đóng góp, nullable. */
    @SerializedName("contributionId") val contributionId: String?,
    /** UUID của mục tiêu được đóng góp, nullable. */
    @SerializedName("goalId") val goalId: String?,
    /** Số tiền đã đóng góp (VNĐ), nullable. */
    @SerializedName("amount") val amount: Long?,
    /** Tổng số tiền mục tiêu sau khi đóng góp (VNĐ), nullable. */
    @SerializedName("currentGoalAmount") val currentGoalAmount: Long?,
    /** Số dư ví hiện tại sau khi đóng góp (VNĐ), nullable.
     * Chỉ có giá trị khi đóng góp từ ví. */
    @SerializedName("currentWalletBalance") val currentWalletBalance: Long?,
    /** UUID của người đóng góp, nullable. */
    @SerializedName("contributorId") val contributorId: String?
)
