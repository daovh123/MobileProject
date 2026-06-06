package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.analytics.CategoryBreakdownDto
import com.example.mobileproject.data.model.analytics.SpendingTrendDto
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.LogoutResponseDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.data.model.favorite.FavoriteListResponseDto
import com.example.mobileproject.data.model.favorite.FavoriteToggleResponseDto
import com.example.mobileproject.data.model.favorite.HistoryListResponseDto
import com.example.mobileproject.data.model.goal.ContributeDirectRequestDto
import com.example.mobileproject.data.model.goal.ContributeFromWalletRequestDto
import com.example.mobileproject.data.model.goal.ContributionResponseDto
import com.example.mobileproject.data.model.goal.CreateGoalRequestDto
import com.example.mobileproject.data.model.goal.GoalDto
import com.example.mobileproject.data.model.map.MapLastLocationsResponseDto
import com.example.mobileproject.data.model.notification.AppNotificationDto
import com.example.mobileproject.data.model.notification.FcmTokenRequestDto
import com.example.mobileproject.data.model.notification.NotificationPageDto
import com.example.mobileproject.data.model.notification.UnreadCountDto
import com.example.mobileproject.data.model.onboarding.AvatarFrameDto
import com.example.mobileproject.data.model.onboarding.AvatarFrameRequestDto
import com.example.mobileproject.data.model.onboarding.AvatarUploadResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestActionResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.CouplePartnerProfileResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleStatusResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.data.model.moment.MomentCommentDto
import com.example.mobileproject.data.model.moment.MomentCommentRequestDto
import com.example.mobileproject.data.model.moment.MomentReactionRequestDto
import com.example.mobileproject.data.model.moment.MomentReactionResponseDto
import com.example.mobileproject.data.model.moment.MomentRequestDto
import com.example.mobileproject.data.model.moment.MomentResponseDto
import com.example.mobileproject.data.model.ProductDto
import okhttp3.MultipartBody
import com.example.mobileproject.data.model.remote.WalletResponse
import com.example.mobileproject.data.model.transaction.IncomeRequestDto
import com.example.mobileproject.data.model.transaction.TopUpCreateRequestDto
import com.example.mobileproject.data.model.transaction.TopUpResponseDto
import com.example.mobileproject.data.model.transaction.PayoutCreateRequestDto
import com.example.mobileproject.data.model.transaction.PayoutResponseDto
import com.example.mobileproject.data.model.transaction.TransactionDto
import com.example.mobileproject.data.model.transaction.TransactionRequestDto
import com.example.mobileproject.data.model.transaction.TransactionResponseDto
import retrofit2.http.*
import retrofit2.Response

/**
 * Retrofit API service chính của ứng dụng, chứa tất cả các endpoint trừ Place và Goal chuyên biệt.
 *
 * ## Xác thực
 * Hầu hết các endpoint yêu cầu header `Authorization: Bearer <token>`.
 * Các endpoint auth (login/register) không yêu cầu token.
 *
 * ## Base URL
 * Được cấu hình trong Hilt/Retrofit module, các đường dẫn bên dưới là relative.
 */
interface ApiService {

    // ===================== Product APIs =====================

    /**
     * Lấy danh sách sản phẩm.
     *
     * - **HTTP**: `GET products`
     * - **Auth**: Không yêu cầu
     * - **Response**: [List]<[ProductDto]>
     * - **Mục đích**: Hiển thị danh sách sản phẩm mẫu/demo
     */
    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    // ===================== Auth APIs =====================

    /**
     * Đăng nhập bằng username hoặc email.
     *
     * - **HTTP**: `POST api/auth/login`
     * - **Auth**: Không yêu cầu
     * - **Request body**: [LoginRequestDto] (usernameOrEmail, password)
     * - **Response**: [AuthResponseDto] chứa token, username, email, trạng thái profile
     * - **Mục đích**: Xác thực người dùng, lấy JWT token
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    /**
     * Đăng ký tài khoản mới.
     *
     * - **HTTP**: `POST api/auth/register`
     * - **Auth**: Không yêu cầu
     * - **Request body**: [RegisterRequestDto] (username, email, password)
     * - **Response**: [AuthResponseDto] chứa token và thông tin tài khoản
     * - **Mục đích**: Tạo tài khoản mới, trả về token để tự động đăng nhập
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    /**
     * Đăng xuất, vô hiệu hóa token hiện tại.
     *
     * - **HTTP**: `POST api/auth/logout`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [LogoutResponseDto]
     * - **Mục đích**: Đăng xuất phía server, client cần xóa local session
     */
    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String,
    ): Response<LogoutResponseDto>

    // ===================== Profile APIs =====================

    /**
     * Tạo hoặc cập nhật hồ sơ người dùng.
     *
     * - **HTTP**: `PUT api/auth/profile`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [ProfileUpsertRequestDto] (fullName, nickName, birthDate, gender, email)
     * - **Response**: [ProfileResponseDto] chứa thông tin hồ sơ đã cập nhật
     * - **Mục đích**: Onboarding bước 2 - lưu thông tin cá nhân
     */
    @PUT("api/auth/profile")
    suspend fun upsertProfile(
        @Header("Authorization") authorization: String,
        @Body request: ProfileUpsertRequestDto,
    ): Response<ProfileResponseDto>

    /**
     * Lấy thông tin hồ sơ người dùng hiện tại.
     *
     * - **HTTP**: `GET api/auth/profile`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [ProfileResponseDto]
     * - **Mục đích**: Hiển thị hồ sơ cá nhân, kiểm tra trạng thái hoàn thành
     */
    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String,
    ): Response<ProfileResponseDto>

    // ===================== Couple APIs =====================

    /**
     * Lấy trạng thái ghép đôi hiện tại.
     *
     * - **HTTP**: `GET api/auth/couple/status`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [CoupleStatusResponseDto] chứa trạng thái ghép đôi, couple code, yêu cầu đang chờ
     * - **Mục đích**: Kiểm tra đã ghép đôi chưa, có yêu cầu chờ xử lý không
     */
    @GET("api/auth/couple/status")
    suspend fun getCoupleStatus(
        @Header("Authorization") authorization: String,
    ): Response<CoupleStatusResponseDto>

    /**
     * Lấy thông tin tóm tắt hồ sơ đối phương.
     *
     * - **HTTP**: `GET api/auth/couple/partner-profile`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [CouplePartnerProfileResponseDto]
     * - **Mục đích**: Hiển thị thông tin đối phương trên trang couple
     */
    @GET("api/auth/couple/partner-profile")
    suspend fun getPartnerProfile(
        @Header("Authorization") authorization: String,
    ): Response<CouplePartnerProfileResponseDto>

    /**
     * Gửi yêu cầu ghép đôi đến đối phương.
     *
     * - **HTTP**: `POST api/auth/couple/requests`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [CoupleRequestCreateRequestDto] (partnerCode)
     * - **Response**: [CoupleRequestActionResponseDto]
     * - **Mục đích**: Khởi tạo yêu cầu ghép đôi bằng mã đối phương
     */
    @POST("api/auth/couple/requests")
    suspend fun sendCoupleRequest(
        @Header("Authorization") authorization: String,
        @Body request: CoupleRequestCreateRequestDto,
    ): Response<CoupleRequestActionResponseDto>

    /**
     * Chấp nhận hoặc từ chối yêu cầu ghép đôi.
     *
     * - **HTTP**: `POST api/auth/couple/requests/{requestId}/decision`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `requestId` - ID của yêu cầu ghép đôi
     * - **Request body**: [CoupleRequestDecisionRequestDto] (accept: Boolean)
     * - **Response**: [CoupleRequestActionResponseDto]
     * - **Mục đích**: Xử lý yêu cầu ghép đôi đến (chấp nhận/từ chối)
     */
    @POST("api/auth/couple/requests/{requestId}/decision")
    suspend fun decideCoupleRequest(
        @Header("Authorization") authorization: String,
        @Path("requestId") requestId: String,
        @Body request: CoupleRequestDecisionRequestDto,
    ): Response<CoupleRequestActionResponseDto>

    // ===================== Favorite APIs =====================

    /**
     * Thêm/xóa địa điểm yêu thích (toggle).
     *
     * - **HTTP**: `POST api/favorites/toggle`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query param**: `placeId` - ID địa điểm
     * - **Response**: [FavoriteToggleResponseDto] chứa message thông báo thêm/xóa
     * - **Mục đích**: Toggle trạng thái yêu thích, response message chứa "Added" nếu thêm
     */
    @POST("api/favorites/toggle")
    suspend fun toggleFavorite(
        @Header("Authorization") authorization: String,
        @Query("placeId") placeId: String,
    ): Response<FavoriteToggleResponseDto>

    /**
     * Lấy danh sách địa điểm yêu thích.
     *
     * - **HTTP**: `GET api/favorites`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [FavoriteListResponseDto] chứa danh sách places
     * - **Mục đích**: Hiển thị danh sách yêu thích trên trang Favorites
     */
    @GET("api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") authorization: String,
    ): Response<FavoriteListResponseDto>

    /**
     * Kiểm tra một địa điểm có trong danh sách yêu thích không.
     *
     * - **HTTP**: `GET api/favorites/check`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query param**: `placeId` - ID địa điểm
     * - **Response**: [FavoriteToggleResponseDto] với `success` = true/false
     * - **Mục đích**: Hiển thị đúng icon trái tim trên UI khi xem chi tiết địa điểm
     */
    @GET("api/favorites/check")
    suspend fun checkFavorite(
        @Header("Authorization") authorization: String,
        @Query("placeId") placeId: String,
    ): Response<FavoriteToggleResponseDto>

    // ===================== History APIs =====================

    /**
     * Ghi nhận lượt xem địa điểm vào lịch sử.
     *
     * - **HTTP**: `POST api/history/view`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query param**: `placeId` - ID địa điểm đã xem
     * - **Response**: `Unit` (200 OK)
     * - **Mục đích**: Lưu lịch sử xem địa điểm của người dùng
     */
    @POST("api/history/view")
    suspend fun recordHistory(
        @Header("Authorization") authorization: String,
        @Query("placeId") placeId: String,
    ): Response<Unit>

    /**
     * Lấy danh sách lịch sử xem địa điểm.
     *
     * - **HTTP**: `GET api/history`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [HistoryListResponseDto] chứa danh sách places đã xem
     * - **Mục đích**: Hiển thị lịch sử xem địa điểm
     */
    @GET("api/history")
    suspend fun getHistory(
        @Header("Authorization") authorization: String,
    ): Response<HistoryListResponseDto>

    /**
     * Lấy vị trí gần nhất đã lưu trên bản đồ.
     *
     * - **HTTP**: `GET api/auth/map/last`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [MapLastLocationsResponseDto]
     * - **Mục đích**: Khôi phục vị trí camera bản đồ trang chủ từ lần trước
     */
    @GET("api/auth/map/last")
    suspend fun getMapLastLocations(
        @Header("Authorization") authorization: String,
    ): Response<MapLastLocationsResponseDto>

    /**
     * Xóa toàn bộ lịch sử xem địa điểm.
     *
     * - **HTTP**: `DELETE api/history`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: `Unit` (200 OK)
     * - **Mục đích**: Cho phép người dùng xóa lịch sử xem
     */
    @DELETE("api/history")
    suspend fun clearHistory(
        @Header("Authorization") authorization: String,
    ): Response<Unit>

    // ===================== Notification APIs =====================

    /**
     * Đăng ký FCM token để nhận thông báo đẩy.
     *
     * - **HTTP**: `POST api/notifications/fcm-token`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [FcmTokenRequestDto] (token: String)
     * - **Response**: `Unit` (200 OK)
     * - **Mục đích**: Gửi FCM token lên server để server có thể gửi push notification
     */
    @POST("api/notifications/fcm-token")
    suspend fun registerFcmToken(
        @Header("Authorization") authorization: String,
        @Body request: FcmTokenRequestDto,
    ): Response<Unit>

    /**
     * Lấy danh sách thông báo (phân trang).
     *
     * - **HTTP**: `GET api/notifications`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query param**: `page` - số trang (mặc định 0)
     * - **Response**: [NotificationPageDto] chứa danh sách thông báo phân trang
     * - **Mục đích**: Hiển thị danh sách thông báo trên trang Notifications
     */
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 0,
    ): Response<NotificationPageDto>

    /**
     * Lấy số lượng thông báo chưa đọc.
     *
     * - **HTTP**: `GET api/notifications/unread-count`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [UnreadCountDto] (count: Int)
     * - **Mục đích**: Hiển thị badge số thông báo chưa đọc trên tab bar
     */
    @GET("api/notifications/unread-count")
    suspend fun getUnreadNotificationCount(
        @Header("Authorization") authorization: String,
    ): Response<UnreadCountDto>

    /**
     * Đánh dấu tất cả thông báo đã đọc.
     *
     * - **HTTP**: `PUT api/notifications/read-all`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: `Unit` (200 OK)
     * - **Mục đích**: Đánh dấu tất cả đã đọc khi người dùng mở trang Notifications
     */
    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsRead(
        @Header("Authorization") authorization: String,
    ): Response<Unit>

    /**
     * Đánh dấu một thông báo đã đọc.
     *
     * - **HTTP**: `PUT api/notifications/{id}/read`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `id` - ID thông báo
     * - **Response**: `Unit` (200 OK)
     * - **Mục đích**: Đánh dấu đã đọc khi người dùng nhấn vào một thông báo
     */
    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") authorization: String,
        @Path("id") notificationId: String,
    ): Response<Unit>

    // ===================== Wallet APIs =====================

    /**
     * Lấy thông tin ví chung của cặp đôi.
     *
     * - **HTTP**: `GET api/v1/wallet/{coupleId}`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `coupleId` - ID cặp đôi (encoded)
     * - **Response**: [WalletResponse] (idCouple, walletName, totalBalance)
     * - **Mục đích**: Hiển thị số dư ví chung trên trang Home/Wallet
     */
    @GET("api/v1/wallet/{coupleId}")
    suspend fun getWallet(
        @Header("Authorization") authorization: String,
        @Path("coupleId", encoded = true) coupleId: String
    ): Response<WalletResponse>

    // ===================== Top-up APIs =====================

    /**
     * Tạo yêu cầu nạp tiền vào ví.
     *
     * - **HTTP**: `POST api/v1/top-ups`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [TopUpCreateRequestDto] (coupleId, amount, bankId, bankName, note)
     * - **Response**: [TopUpResponseDto] chứa thông tin yêu cầu nạp tiền
     * - **Mục đích**: Khởi tạo giao dịch nạp tiền, trả về thông tin để hiển thị
     */
    @POST("api/v1/top-ups")
    suspend fun createTopUp(
        @Header("Authorization") authorization: String,
        @Body request: TopUpCreateRequestDto,
    ): Response<TopUpResponseDto>

    /**
     * Lấy thông tin chi tiết một yêu cầu nạp tiền.
     *
     * - **HTTP**: `GET api/v1/top-ups/{id}`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `id` - ID yêu cầu nạp tiền
     * - **Response**: [TopUpResponseDto]
     * - **Mục đích**: Kiểm tra trạng thái yêu cầu nạp tiền (polling)
     */
    @GET("api/v1/top-ups/{id}")
    suspend fun getTopUp(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): Response<TopUpResponseDto>

    // ===================== Payout APIs =====================

    /**
     * Tạo yêu cầu rút tiền từ ví.
     *
     * - **HTTP**: `POST api/v1/payouts`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [PayoutCreateRequestDto] (coupleId, amount)
     * - **Response**: [PayoutResponseDto] chứa thông tin yêu cầu rút tiền
     * - **Mục đích**: Khởi tạo giao dịch rút tiền từ ví chung
     */
    @POST("api/v1/payouts")
    suspend fun createPayout(
        @Header("Authorization") authorization: String,
        @Body request: PayoutCreateRequestDto,
    ): Response<PayoutResponseDto>

    /**
     * Lấy thông tin chi tiết một yêu cầu rút tiền.
     *
     * - **HTTP**: `GET api/v1/payouts/{id}`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `id` - ID yêu cầu rút tiền
     * - **Response**: [PayoutResponseDto]
     * - **Mục đích**: Kiểm tra trạng thái yêu cầu rút tiền (polling)
     */
    @GET("api/v1/payouts/{id}")
    suspend fun getPayout(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): Response<PayoutResponseDto>

    // ===================== Transaction APIs =====================

    /**
     * Tạo giao dịch chi tiêu mới.
     *
     * - **HTTP**: `POST api/v1/transactions`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [TransactionRequestDto] (coupleId, amount, type, category, note)
     * - **Response**: [TransactionResponseDto] chứa thông tin giao dịch và số dư mới
     * - **Mục đích**: Ghi nhận giao dịch chi tiêu, cập nhật số dư ví
     */
    @POST("api/v1/transactions")
    suspend fun createTransaction(
        @Header("Authorization") authorization: String,
        @Body request: TransactionRequestDto,
    ): Response<TransactionResponseDto>

    /**
     * Xử lý giao dịch thu nhập (tiền vào).
     *
     * - **HTTP**: `POST api/v1/transactions/income`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [IncomeRequestDto] (coupleId, amount, targetType, goalId, note)
     * - **Response**: [TransactionResponseDto] chứa thông tin giao dịch và số dư mới
     * - **Mục đích**: Ghi nhận thu nhập, có thể chuyển thẳng vào mục tiêu (goal)
     */
    @POST("api/v1/transactions/income")
    suspend fun processIncome(
        @Header("Authorization") authorization: String,
        @Body request: IncomeRequestDto,
    ): Response<TransactionResponseDto>

    /**
     * Lấy danh sách giao dịch của cặp đôi.
     *
     * - **HTTP**: `GET api/v1/transactions`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query param**: `coupleId` - ID cặp đôi
     * - **Response**: [List]<[TransactionDto]>
     * - **Mục đích**: Hiển thị lịch sử giao dịch trên trang Transactions
     */
    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @Query("coupleId") coupleId: String,
    ): Response<List<TransactionDto>>

    // ===================== Goal APIs =====================

    /**
     * Tạo mục tiêu tài chính mới.
     *
     * - **HTTP**: `POST api/v1/goals`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [CreateGoalRequestDto] (coupleId, name, category, type, targetAmount, deadline, tasks)
     * - **Response**: [GoalDto] chứa thông tin mục tiêu đã tạo
     * - **Mục đích**: Tạo mục tiêu tiết kiệm/chi tiêu cho cặp đôi
     */
    @POST("api/v1/goals")
    suspend fun createGoal(
        @Header("Authorization") authorization: String,
        @Body request: CreateGoalRequestDto,
    ): Response<GoalDto>

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
        @Path("coupleId") coupleId: String,
    ): Response<List<GoalDto>>

    /**
     * Đóng góp vào mục tiêu từ ví chung.
     *
     * - **HTTP**: `POST api/v1/goals/{goalId}/contribute-from-wallet`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `goalId` - ID mục tiêu
     * - **Request body**: [ContributeFromWalletRequestDto] (amount, note)
     * - **Response**: [ContributionResponseDto] chứa số tiền đã đóng góp và số dư ví mới
     * - **Mục đích**: Chuyển tiền từ ví chung vào mục tiêu
     */
    @POST("api/v1/goals/{goalId}/contribute-from-wallet")
    suspend fun contributeFromWallet(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeFromWalletRequestDto,
    ): Response<ContributionResponseDto>

    /**
     * Đóng góp trực tiếp vào mục tiêu (không qua ví).
     *
     * - **HTTP**: `POST api/v1/goals/{goalId}/contribute`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `goalId` - ID mục tiêu
     * - **Request body**: [ContributeDirectRequestDto] (amount, contributorId, note)
     * - **Response**: [ContributionResponseDto]
     * - **Mục đích**: Ghi nhận đóng góp trực tiếp (tiền mặt/ngoại tuyến)
     */
    @POST("api/v1/goals/{goalId}/contribute")
    suspend fun contributeToGoal(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String,
        @Body request: ContributeDirectRequestDto,
    ): Response<ContributionResponseDto>

    // ===================== Avatar APIs =====================

    /**
     * Tải lên ảnh đại diện (avatar).
     *
     * - **HTTP**: `POST api/auth/profile/avatar` (multipart)
     * - **Auth**: Yêu cầu Bearer token
     * - **Request**: [MultipartBody.Part] (field name: "file")
     * - **Response**: [AvatarUploadResponseDto] chứa avatarUrl mới
     * - **Mục đích**: Cập nhật ảnh đại diện người dùng
     */
    @Multipart
    @POST("api/auth/profile/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
    ): Response<AvatarUploadResponseDto>

    /**
     * Lấy danh sách khung avatar có sẵn.
     *
     * - **HTTP**: `GET api/auth/profile/frames`
     * - **Auth**: Yêu cầu Bearer token
     * - **Response**: [List]<[AvatarFrameDto]>
     * - **Mục đích**: Hiển thị danh sách khung avatar để người dùng chọn
     */
    @GET("api/auth/profile/frames")
    suspend fun getAvatarFrames(
        @Header("Authorization") authorization: String,
    ): Response<List<AvatarFrameDto>>

    /**
     * Đặt khung avatar cho hồ sơ.
     *
     * - **HTTP**: `PUT api/auth/profile/frame`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [AvatarFrameRequestDto] (frameId: String?)
     * - **Response**: [ProfileResponseDto] chứa hồ sơ đã cập nhật
     * - **Mục đích**: Thay đổi hoặc xóa khung avatar (frameId = null để xóa)
     */
    @PUT("api/auth/profile/frame")
    suspend fun setAvatarFrame(
        @Header("Authorization") authorization: String,
        @Body request: AvatarFrameRequestDto,
    ): Response<ProfileResponseDto>

    // ===================== Analytics APIs =====================

    /**
     * Lấy phân bổ chi tiêu theo danh mục trong tháng.
     *
     * - **HTTP**: `GET api/v1/analytics/expense-by-category`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query params**: `coupleId`, `month`, `year`
     * - **Response**: [List]<[CategoryBreakdownDto]> (category, totalAmount, percentage)
     * - **Mục đích**: Hiển thị biểu đồ phân bổ chi tiêu theo danh mục (pie chart)
     */
    @GET("api/v1/analytics/expense-by-category")
    suspend fun getCategoryBreakdown(
        @Header("Authorization") authorization: String,
        @Query("coupleId") coupleId: String,
        @Query("month") month: Int,
        @Query("year") year: Int,
    ): Response<List<CategoryBreakdownDto>>

    /**
     * Lấy xu hướng chi tiêu theo tháng trong năm.
     *
     * - **HTTP**: `GET api/v1/analytics/monthly-trend`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query params**: `coupleId`, `year`
     * - **Response**: [List]<[SpendingTrendDto]> (month, totalExpense, totalIncome)
     * - **Mục đích**: Hiển thị biểu đồ xu hướng chi tiêu 12 tháng (line chart)
     */
    @GET("api/v1/analytics/monthly-trend")
    suspend fun getMonthlyTrend(
        @Header("Authorization") authorization: String,
        @Query("coupleId") coupleId: String,
        @Query("year") year: Int
    ): Response<List<SpendingTrendDto>>

    // ===================== Moment APIs =====================

    /**
     * Lấy danh sách khoảnh khắc (moments) của cặp đôi.
     *
     * - **HTTP**: `GET api/v1/moments`
     * - **Auth**: Yêu cầu Bearer token
     * - **Query param**: `coupleId` - ID cặp đôi
     * - **Response**: [List]<[MomentDto]>
     * - **Mục đích**: Hiển thị dòng thời gian khoảnh khắc của cặp đôi
     */
    @GET("api/v1/moments")
    suspend fun getMoments(
        @Query("coupleId") coupleId: String,
        @Header("Authorization") authorization: String,
    ): Response<List<MomentDto>>

    /**
     * Tạo khoảnh khắc mới.
     *
     * - **HTTP**: `POST api/v1/moments`
     * - **Auth**: Yêu cầu Bearer token
     * - **Request body**: [MomentRequestDto] (content, images, ...)
     * - **Response**: [MomentResponseDto] chứa thông tin khoảnh khắc đã tạo
     * - **Mục đích**: Đăng khoảnh khắc mới trên dòng thời gian
     */
    @POST("api/v1/moments")
    suspend fun createMoment(
        @Body request: MomentRequestDto,
        @Header("Authorization") authorization: String,
    ): Response<MomentResponseDto>

    /**
     * Thả cảm xúc (reaction) cho khoảnh khắc.
     *
     * - **HTTP**: `POST api/v1/moments/{momentId}/reactions`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `momentId` - ID khoảnh khắc
     * - **Request body**: [MomentReactionRequestDto] (reactionType: String)
     * - **Response**: [MomentReactionResponseDto]
     * - **Mục đích**: Thả cảm xúc (like, love, ...) cho khoảnh khắc
     */
    @POST("api/v1/moments/{momentId}/reactions")
    suspend fun reactToMoment(
        @Path("momentId") momentId: String,
        @Body request: MomentReactionRequestDto,
        @Header("Authorization") authorization: String,
    ): Response<MomentReactionResponseDto>

    /**
     * Lấy danh sách bình luận của khoảnh khắc.
     *
     * - **HTTP**: `GET api/v1/moments/{momentId}/comments`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `momentId` - ID khoảnh khắc
     * - **Response**: [List]<[MomentCommentDto]>
     * - **Mục đích**: Hiển thị danh sách bình luận khi người dùng mở chi tiết khoảnh khắc
     */
    @GET("api/v1/moments/{momentId}/comments")
    suspend fun getMomentComments(
        @Path("momentId") momentId: String,
        @Header("Authorization") authorization: String,
    ): Response<List<MomentCommentDto>>

    /**
     * Tạo bình luận mới cho khoảnh khắc.
     *
     * - **HTTP**: `POST api/v1/moments/{momentId}/comments`
     * - **Auth**: Yêu cầu Bearer token
     * - **Path param**: `momentId` - ID khoảnh khắc
     * - **Request body**: [MomentCommentRequestDto] (content: String)
     * - **Response**: [MomentCommentDto] chứa bình luận đã tạo
     * - **Mục đích**: Gửi bình luận mới cho khoảnh khắc
     */
    @POST("api/v1/moments/{momentId}/comments")
    suspend fun createMomentComment(
        @Path("momentId") momentId: String,
        @Body request: MomentCommentRequestDto,
        @Header("Authorization") authorization: String,
    ): Response<MomentCommentDto>
}
