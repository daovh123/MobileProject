package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.entity.SavingGoal
import kotlinx.coroutines.flow.Flow

/**
 * Repository quản lý mục tiêu (goals) của cặp đôi.
 *
 * Bao gồm các thao tác: tạo mục tiêu, đóng góp tiền, đánh dấu nhiệm vụ hoàn thành.
 * Sử dụng [Flow] kết hợp [Resource] để phản ánh trạng thái Loading/Success/Error
 * cho các thao tác bất đồng bộ từ domain layer.
 */
interface GoalRepository {

    /**
     * Lấy danh sách tất cả mục tiêu của cặp đôi.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @return Flow phát ra [Resource.Success] chứa danh sách [Goal],
     *         [Resource.Error] khi có lỗi, [Resource.Loading] khi đang tải
     */
    fun getGoals(coupleId: String): Flow<Resource<List<Goal>>>
    
    /**
     * Tạo mục tiêu mới cho cặp đôi.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param name Tên mục tiêu (ví dụ: "Du lịch Đà Lạt")
     * @param category Danh mục mục tiêu (xem [com.example.mobileproject.domain.model.GoalCategory])
     * @param type Loại mục tiêu: "saving" (tiết kiệm) hoặc "task" (nhiệm vụ)
     * @param targetAmount Số tiền mục tiêu (đơn vị: VND), chỉ bắt buộc với loại "saving"
     * @param deadline Hạn hoàn thành dạng ISO-8601 (yyyy-MM-dd), null nếu không đặt hạn
     * @param tasks Danh sách nhiệm vụ con, chỉ áp dụng với loại "task"
     * @return Flow phát ra [Resource.Success] chứa [Goal] vừa tạo,
     *         [Resource.Error] khi validation thất bại hoặc lỗi server
     */
    fun createGoal(
        coupleId: String,
        name: String,
        category: String,
        type: String,
        targetAmount: Long? = null,
        deadline: String? = null,
        tasks: List<com.example.mobileproject.domain.entity.GoalTask>? = null
    ): Flow<Resource<Goal>>

    /**
     * Đánh dấu một nhiệm vụ (task) trong mục tiêu là hoàn thành/chưa hoàn thành (toggle).
     *
     * @param goalId ID mục tiêu chứa nhiệm vụ
     * @param taskId ID nhiệm vụ cần toggle
     * @return Flow phát ra [Resource.Success] chứa tiến độ mới (0.0 - 100.0),
     *         [Resource.Error] khi goal/task không tồn tại
     */
    fun toggleTask(goalId: String, taskId: String): Flow<Resource<Double>>

    /**
     * Đóng góp tiền vào mục tiêu từ ví chung của cặp đôi.
     *
     * Tiền sẽ được trừ trực tiếp khỏi số dư ví chung.
     *
     * @param goalId ID mục tiêu cần đóng góp
     * @param amount Số tiền đóng góp (đơn vị: VND), phải lớn hơn 0
     * @param note Ghi chú cho giao dịch, null nếu không có
     * @return Flow phát ra [Resource.Success] chứa [GoalContributionResult] với thông tin cập nhật,
     *         [Resource.Error] khi số dư ví không đủ hoặc goal không tồn tại
     */
    fun contributeFromWallet(
        goalId: String,
        amount: Long,
        note: String?
    ): Flow<Resource<GoalContributionResult>>

    /**
     * Đóng góp tiền trực tiếp vào mục tiêu từ một thành viên cụ thể.
     *
     * Khác với [contributeFromWallet], phương thức này cho phép một thành viên
     * đóng góp mà không thông qua ví chung (ví dụ: nạp tiền mặt).
     *
     * @param goalId ID mục tiêu cần đóng góp
     * @param amount Số tiền đóng góp (đơn vị: VND), phải lớn hơn 0
     * @param contributorId ID của thành viên thực hiện đóng góp
     * @param note Ghi chú cho giao dịch, null nếu không có
     * @return Flow phát ra [Resource.Success] chứa [GoalContributionResult],
     *         [Resource.Error] khi contributorId không hợp lệ hoặc goal không tồn tại
     */
    fun contributeDirect(
        goalId: String,
        amount: Long,
        contributorId: String,
        note: String?
    ): Flow<Resource<GoalContributionResult>>
}
