package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.goal.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service chuyên biệt cho các endpoint mục tiêu tài chính (Goal).
 *
 * ## Đặc điểm
 * - Tất cả endpoint đều yêu cầu authentication (Bearer token)
 * - Tách riêng khỏi [ApiService] để giảm coupling và dễ quản lý
 * - Hỗ trợ CRUD mục tiêu, đóng góp, và quản lý task con
 */
interface GoalApiService {

    /**
     * Tạo mục tiêu tài chính mới.
     *
     * - **HTTP**: `POST api/v1/goals`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [CreateGoalRequestDto] (coupleId, name, category, type, targetAmount, deadline, tasks)
     * - **Response**: [GoalDto] chứa thông tin mục tiêu đã tạo
     * - **Mục đích**: Tạo mục tiêu tiết kiệm/chi tiêu mới cho cặp đôi
     */
    @POST("api/v1/goals")
    suspend fun createGoal(
        @Header("Authorization") authorization: String,
        @Body request: CreateGoalRequestDto
    ): Response<GoalDto>

    /**
     * Bật/tắt trạng thái hoàn thành của một task trong mục tiêu.
     *
     * - **HTTP**: `PATCH api/v1/goals/{goalId}/tasks/{taskId}`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path params**: `goalId` - ID mục tiêu, `taskId` - ID task
     * - **Response**: [ToggleTaskResponseDto] chứa progress mới (% hoàn thành)
     * - **Mục đích**: Đánh dấu task hoàn thành/chưa hoàn thành, cập nhật tiến độ mục tiêu
     */
    @PATCH("api/v1/goals/{goalId}/tasks/{taskId}")
    suspend fun toggleTask(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Path("taskId") taskId: String
    ): Response<ToggleTaskResponseDto>

    /**
     * Lấy danh sách mục tiêu theo cặp đôi.
     *
     * - **HTTP**: `GET api/v1/goals/couple/{coupleId}`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `coupleId` - ID cặp đôi
     * - **Response**: [List]<[GoalDto]>
     * - **Mục đích**: Hiển thị danh sách mục tiêu trên trang Goals
     */
    @GET("api/v1/goals/couple/{coupleId}")
    suspend fun getGoalsByCouple(
        @Header("Authorization") authorization: String,
        @Path("coupleId") coupleId: String
    ): Response<List<GoalDto>>

    /**
     * Lấy chi tiết một mục tiêu theo ID.
     *
     * - **HTTP**: `GET api/v1/goals/{goalId}`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `goalId` - ID mục tiêu
     * - **Response**: [GoalDto]
     * - **Mục đích**: Hiển thị chi tiết mục tiêu và danh sách task
     */
    @GET("api/v1/goals/{goalId}")
    suspend fun getGoalById(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String
    ): Response<GoalDto>

    /**
     * Đóng góp vào mục tiêu từ ví chung.
     *
     * - **HTTP**: `POST api/v1/goals/{goalId}/contribute-from-wallet`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `goalId` - ID mục tiêu
     * - **Request body**: [ContributeFromWalletRequestDto] (amount, note)
     * - **Response**: [ContributionResponseDto] (success, amount, currentGoalAmount, currentWalletBalance)
     * - **Mục đích**: Chuyển tiền từ ví chung vào mục tiêu, trừ số dư ví
     */
    @POST("api/v1/goals/{goalId}/contribute-from-wallet")
    suspend fun contributeFromWallet(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeFromWalletRequestDto
    ): Response<ContributionResponseDto>

    /**
     * Đóng góp trực tiếp vào mục tiêu (không qua ví).
     *
     * - **HTTP**: `POST api/v1/goals/{goalId}/contribute`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `goalId` - ID mục tiêu
     * - **Request body**: [ContributeDirectRequestDto] (amount, contributorId, note)
     * - **Response**: [ContributionResponseDto]
     * - **Mục đích**: Ghi nhận đóng góp trực tiếp (tiền mặt/ngoại tuyến), không ảnh hưởng ví
     */
    @POST("api/v1/goals/{goalId}/contribute")
    suspend fun contributeDirect(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeDirectRequestDto
    ): Response<ContributionResponseDto>
}
