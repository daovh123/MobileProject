package com.example.mobileproject.domain.entity

/**
 * Sealed class đại diện cho mục tiêu chung của cặp đôi.
 * Sử dụng sealed class để hỗ trợ polymorphism — mỗi loại mục tiêu có cấu trúc dữ liệu riêng
 * nhưng chia sẻ các thuộc tính cơ bản như id, tên, trạng thái.
 *
 * Các loại mục tiêu:
 * - [SavingGoal]: Mục tiêu tiết kiệm, có số tiền mục tiêu và theo dõi tiến độ bằng số tiền
 * - [FutureGoal]: Mục tiêu tương lai, có danh sách công việc và theo dõi tiến độ bằng phần trăm
 *
 * @property id Định danh duy nhất của mục tiêu
 * @property coupleId Định danh của cặp đôi sở hữu mục tiêu này
 * @property name Tên mục tiêu do người dùng đặt (ví dụ: "Mua xe máy", "Đi Đà Lạt")
 * @property category Phân loại mục tiêu (ví dụ: "Du lịch", "Mua sắm", "Tiết kiệm")
 * @property deadline Thời hạn hoàn thành dạng chuỗi ISO, null nếu không đặt thời hạn
 * @property status Trạng thái hiện tại của mục tiêu (đang thực hiện / đã đạt / thất bại)
 * @property createdAt Thời điểm tạo mục tiêu, dùng để hiển thị và sắp xếp
 * @property type Loại mục tiêu, phân biệt giữa Saving và Future
 */
sealed class Goal(
    open val id: String,
    open val coupleId: String,
    open val name: String,
    open val category: String,
    open val deadline: String?,
    open val status: GoalStatus,
    open val createdAt: String?,
    open val type: GoalType
)

/**
 * Mục tiêu tiết kiệm — theo dõi việc tích lũy tiền đến một số tiền mục tiêu.
 * Ví dụ: "Tiết kiệm 10 triệu để đi du lịch".
 *
 * @property targetAmount Số tiền mục tiêu cần đạt (đơn vị: VNĐ)
 * @property currentAmount Số tiền đã tiết kiệm được cho đến hiện tại
 */
data class SavingGoal(
    override val id: String,
    override val coupleId: String,
    override val name: String,
    override val category: String,
    override val deadline: String?,
    override val status: GoalStatus,
    override val createdAt: String?,
    val targetAmount: Long,
    val currentAmount: Long
) : Goal(id, coupleId, name, category, deadline, status, createdAt, GoalType.SAVING)

/**
 * Mục tiêu tương lai — theo dõi việc hoàn thành các công việc con (tasks).
 * Ví dụ: "Chu bị đám cưới" với các task như "Đặt nhà hàng", "Chụp ảnh cưới".
 *
 * @property tasks Danh sách công việc cần hoàn thành để đạt mục tiêu
 * @property progress Tiến độ hoàn thành dưới dạng phần trăm (0.0 - 1.0), dựa trên tỷ lệ task đã xong
 */
data class FutureGoal(
    override val id: String,
    override val coupleId: String,
    override val name: String,
    override val category: String,
    override val deadline: String?,
    override val status: GoalStatus,
    override val createdAt: String?,
    val tasks: List<GoalTask>,
    val progress: Double
) : Goal(id, coupleId, name, category, deadline, status, createdAt, GoalType.FUTURE)

/**
 * Một công việc con (task) thuộc mục tiêu tương lai [FutureGoal].
 *
 * @property taskId Định danh duy nhất của công việc
 * @property content Nội dung mô tả công việc cần làm
 * @property isCompleted True nếu công việc đã được đánh dấu hoàn thành
 */
data class GoalTask(
    val taskId: String,
    val content: String,
    val isCompleted: Boolean
)

/**
 * Enum phân loại loại mục tiêu.
 *
 * - SAVING: Mục tiêu tiết kiệm tiền, theo dõi bằng số tiền tích lũy
 * - FUTURE: Mục tiêu tương lai, theo dõi bằng danh sách công việc
 */
enum class GoalType {
    SAVING, FUTURE
}

/**
 * Enum đại diện cho trạng thái của mục tiêu.
 *
 * - IN_PROGRESS: Mục tiêu đang được thực hiện (mặc định)
 * - ACHIEVED: Mục tiêu đã đạt được (đủ tiền hoặc hoàn thành tất cả task)
 * - FAILED: Mục tiêu thất bại (quá hạn mà không hoàn thành)
 */
enum class GoalStatus {
    IN_PROGRESS,
    ACHIEVED,
    FAILED;

    companion object {
        /**
         * Chuyển đổi chuỗi trạng thái từ API thành enum [GoalStatus].
         * Mặc định trả về [IN_PROGRESS] nếu chuỗi không khớp.
         */
        fun fromString(status: String?): GoalStatus = when (status?.uppercase()) {
            "ACHIEVED" -> ACHIEVED
            "FAILED" -> FAILED
            else -> IN_PROGRESS
        }
    }
}

/**
 * Kết quả của hành động đóng góp tiền vào mục tiêu tiết kiệm.
 * Được trả về sau khi người dùng thực hiện giao dịch góp tiền.
 *
 * @property success True nếu đóng góp thành công
 * @property message Thông điệp phản hồi từ server (thành công hoặc lý do thất bại)
 * @property goalId Định danh mục tiêu được đóng góp
 * @property contributionId Định danh duy nhất của lần đóng góp, dùng để truy vết giao dịch
 * @property amount Số tiền đã đóng góp trong lần này (đơn vị: VNĐ)
 * @property currentAmount Tổng số tiền đã tiết kiệm sau khi đóng góp
 * @property walletBalance Số dư ví còn lại sau giao dịch, null nếu không có thông tin
 * @property timestamp Thời điểm đóng góp dạng chuỗi ISO
 */
data class GoalContributionResult(
    val success: Boolean,
    val message: String,
    val goalId: String,
    val contributionId: String,
    val amount: Long,
    val currentAmount: Long,
    val walletBalance: Long?,
    val timestamp: String
)
