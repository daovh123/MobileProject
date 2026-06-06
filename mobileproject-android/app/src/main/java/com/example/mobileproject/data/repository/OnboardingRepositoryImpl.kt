package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.onboarding.*
import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject

/**
 * Implementation của [OnboardingRepository], xử lý toàn bộ quy trình onboarding:
 * hồ sơ cá nhân, ghép đôi, avatar.
 *
 * ## Caching strategy
 * - Cập nhật [AuthSessionStore] mỗi khi lấy/cập nhật profile hoặc couple status
 * - Không cache response API, luôn fetch mới để đảm bảo dữ liệu đồng bộ
 *
 * ## Error handling
 * - Sử dụng extension `requireSuccessfulBody()` để parse response và throw nếu lỗi
 * - Parse error body JSON để lấy message chi tiết từ server
 * - Xử lý riêng lỗi 401 (phiên hết hạn) với message tiếng Việt
 *
 * ## Data transformation
 * - [ProfileResponseDto] -> [ProfileResult] qua extension `toProfileResult()`
 * - [AvatarFrameDto] -> [AvatarFrame] qua extension `toDomain()`
 * - [CoupleStatusResponseDto] -> [CoupleStatus] mapping thủ công
 *
 * ## Threading
 * Tất cả hàm `suspend` chạy trên IO dispatcher (Retrofit default).
 */
class OnboardingRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
    private val gson: Gson,
) : OnboardingRepository {

    /**
     * Tạo header xác thực từ token.
     *
     * @param token JWT token
     * @return "Bearer <token>"
     */
    private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"

    /**
     * Lưu hoặc cập nhật hồ sơ người dùng.
     *
     * Sau khi lưu thành công, cập nhật [AuthSessionStore] với trạng thái mới.
     *
     * @param token JWT token
     * @param fullName họ và tên
     * @param nickName biệt danh (nullable)
     * @param birthDate ngày sinh (format: yyyy-MM-dd)
     * @param gender giới tính
     * @param email email (nullable)
     * @return [ProfileResult] thông tin hồ sơ đã cập nhật
     * @throws IllegalStateException nếu lưu thất bại
     */
    override suspend fun saveProfile(
        token: String,
        fullName: String,
        nickName: String?,
        birthDate: String,
        gender: String,
        email: String?,
    ): ProfileResult {
        val response = apiService.upsertProfile(
            authorizationHeader(token),
            ProfileUpsertRequestDto(fullName, nickName, birthDate, gender, email)
        )
        val body = response.requireSuccessfulBody(gson, "Luu ho so that bai")
        authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected, null)
        return body.toProfileResult()
    }

    /**
     * Lấy thông tin hồ sơ người dùng hiện tại.
     *
     * Cập nhật [AuthSessionStore] sau khi fetch thành công.
     *
     * @param token JWT token
     * @return [ProfileResult]
     * @throws IllegalStateException nếu fetch thất bại
     */
    override suspend fun getProfile(token: String): ProfileResult {
        val response = apiService.getProfile(authorizationHeader(token))
        val body = response.requireSuccessfulBody(gson, "Khong the lay ho so")
        authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected, null)
        return body.toProfileResult()
    }

    /**
     * Lấy trạng thái ghép đôi hiện tại.
     *
     * Cập nhật [AuthSessionStore] với coupleId và trạng thái ghép đôi.
     * Xử lý riêng lỗi 401 (phiên hết hạn) với message tiếng Việt.
     *
     * @param token JWT token
     * @return [CoupleStatus] trạng thái ghép đôi
     * @throws IllegalStateException nếu fetch thất bại
     */
    override suspend fun getCoupleStatus(token: String): CoupleStatus {
        val response = apiService.getCoupleStatus(authorizationHeader(token))
        val body = response.body()
        if (response.isSuccessful && body != null) {
            // Cập nhật Store quan trọng để HomeScreen có dữ liệu coupleId
            authSessionStore.updateProfileState(
                profileCompleted = body.profileCompleted,
                coupleConnected = body.paired,
                coupleId = body.coupleId
            )
            return CoupleStatus(
                profileCompleted = body.profileCompleted,
                paired = body.paired,
                partnerUsername = body.partnerUsername,
                myCoupleCode = body.myCoupleCode,
                myCoupleCodeExpiresAt = body.myCoupleCodeExpiresAt,
                incomingRequestId = body.incomingRequestId,
                incomingRequesterUsername = body.incomingRequesterUsername,
                incomingRequesterDisplayName = body.incomingRequesterDisplayName,
                incomingCreatedAt = body.incomingCreatedAt,
                outgoingRequestId = body.outgoingRequestId,
                outgoingRecipientUsername = body.outgoingRecipientUsername,
                outgoingStatus = body.outgoingStatus,
                outgoingUpdatedAt = body.outgoingUpdatedAt,
                coupleId = body.coupleId,
                startAt = body.startAt,
                daysTogether = body.daysTogether ?: 0,
                anniversaryTomorrow = body.anniversaryTomorrow ?: false
            )
        } else {
            val errorMsg = if (response.code() == 401) {
                "Phien dang nhap het han, vui long dang nhap lai"
            } else {
                response.parseErrorMessage(gson) ?: "Khong the tai trang thai ghep doi"
            }
            throw Exception(errorMsg)
        }
    }

    /**
     * Lấy thông tin tóm tắt hồ sơ đối phương.
     *
     * @param token JWT token
     * @return [PartnerProfileSummary] thông tin đối phương
     * @throws IllegalStateException nếu fetch thất bại
     */
    override suspend fun getPartnerProfileSummary(token: String): PartnerProfileSummary {
        val response = apiService.getPartnerProfile(authorizationHeader(token))
        val body = response.body()
        if (response.isSuccessful && body != null) {
            return PartnerProfileSummary(
                paired = body.paired,
                message = body.message,
                username = body.username,
                fullName = body.fullName,
                nickName = body.nickName,
                avatarUrl = body.avatarUrl,
                startAt = body.startAt,
                daysTogether = body.daysTogether,
            )
        }
        val errorMsg = response.parseErrorMessage(gson) ?: "Khong the tai thong tin doi phuong"
        throw Exception(errorMsg)
    }

    /**
     * Gửi yêu cầu ghép đôi đến đối phương bằng mã couple code.
     *
     * @param token JWT token
     * @param partnerCode mã ghép đôi của đối phương
     * @return [CoupleRequestAction] kết quả gửi yêu cầu
     * @throws IllegalStateException nếu gửi thất bại
     */
    override suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction {
        val response = apiService.sendCoupleRequest(authorizationHeader(token), CoupleRequestCreateRequestDto(partnerCode))
        val body = response.body()
        if (response.isSuccessful && body != null) {
            return CoupleRequestAction(
                requestId = body.requestId,
                status = body.status,
                message = body.message,
                requesterUsername = body.requesterUsername,
                recipientUsername = body.recipientUsername
            )
        } else {
            throw Exception(response.parseErrorMessage(gson) ?: "Gui yeu cau that bai")
        }
    }

    /**
     * Chấp nhận hoặc từ chối yêu cầu ghép đôi.
     *
     * @param token JWT token
     * @param requestId ID yêu cầu ghép đôi
     * @param accept `true` để chấp nhận, `false` để từ chối
     * @return [CoupleRequestAction] kết quả xử lý
     * @throws IllegalStateException nếu xử lý thất bại
     */
    override suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction {
        val response = apiService.decideCoupleRequest(
            authorizationHeader(token),
            requestId,
            CoupleRequestDecisionRequestDto(accept = accept),
        )
        val body = response.requireSuccessfulBody(gson, "Xu ly yeu cau that bai")
        return CoupleRequestAction(
            requestId = body.requestId,
            status = body.status,
            message = body.message,
            requesterUsername = body.requesterUsername,
            recipientUsername = body.recipientUsername,
        )
    }

    /**
     * Tải lên ảnh đại diện (multipart upload).
     *
     * @param token JWT token
     * @param imageBytes dữ liệu ảnh dạng byte array
     * @param contentType MIME type (ví dụ: "image/jpeg")
     * @return URL avatar mới
     * @throws IllegalStateException nếu upload thất bại
     */
    override suspend fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String): String {
        val requestBody = imageBytes.toRequestBody(contentType.toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", "avatar", requestBody)
        val response = apiService.uploadAvatar(authorizationHeader(token), filePart)
        val body = response.requireAvatarUploadBody(gson, "Tai anh that bai")
        return body.avatarUrl ?: ""
    }

    /**
     * Lấy danh sách khung avatar có sẵn.
     *
     * @param token JWT token
     * @return danh sách [AvatarFrame]
     */
    override suspend fun getAvatarFrames(token: String): List<AvatarFrame> {
        val response = apiService.getAvatarFrames(authorizationHeader(token))
        return response.body()?.map { it.toDomain() } ?: emptyList()
    }

    /**
     * Đặt hoặc xóa khung avatar.
     *
     * @param token JWT token
     * @param frameId ID khung avatar (null để xóa khung hiện tại)
     * @return [ProfileResult] hồ sơ đã cập nhật
     * @throws IllegalStateException nếu đặt khung thất bại
     */
    override suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult {
        val response = apiService.setAvatarFrame(authorizationHeader(token), AvatarFrameRequestDto(frameId))
        val body = response.requireSuccessfulBody(gson, "Dat khung anh that bai")
        return body.toProfileResult()
    }
}

private fun ProfileResponseDto.toProfileResult() = ProfileResult(
    username = username, fullName = fullName, nickName = nickName, birthDate = birthDate,
    gender = gender, profileCompleted = profileCompleted, coupleConnected = coupleConnected,
    email = email, avatarUrl = avatarUrl, avatarFrameId = avatarFrameId,
)

private fun AvatarFrameDto.toDomain() = AvatarFrame(id, name, resourceKey, color)

private fun Response<ProfileResponseDto>.requireSuccessfulBody(gson: Gson, defaultMsg: String): ProfileResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<CoupleStatusResponseDto>.requireSuccessfulBody(gson: Gson, defaultMsg: String): CoupleStatusResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<CoupleRequestActionResponseDto>.requireSuccessfulBody(gson: Gson, defaultMsg: String): CoupleRequestActionResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<AvatarUploadResponseDto>.requireAvatarUploadBody(gson: Gson, defaultMsg: String): AvatarUploadResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<*>.parseErrorMessage(gson: Gson): String? {
    return runCatching { errorBody()?.charStream()?.use { gson.fromJson(it, ErrorMessageDto::class.java).message } }.getOrNull()
}

class ErrorMessageDto(val message: String)
