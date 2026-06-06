package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.domain.entity.ProfileResult

/**
 * Repository quản lý quy trình onboarding và ghép đôi (couple pairing).
 *
 * Bao gồm các thao tác: thiết lập hồ sơ cá nhân, quản lý trạng thái cặp đôi,
 * gửi/nhận yêu cầu ghép đôi, và tùy chỉnh avatar.
 * Đây là repository được sử dụng trong giai đoạn đầu khi người dùng mới đăng ký.
 */
interface OnboardingRepository {

    /**
     * Lưu hoặc cập nhật hồ sơ cá nhân của người dùng.
     *
     * @param token Token xác thực
     * @param fullName Họ và tên đầy đủ
     * @param nickName Tên biệt danh, null nếu không đặt
     * @param birthDate Ngày sinh dạng ISO-8601 (yyyy-MM-dd)
     * @param gender Giới tính: "male", "female", hoặc giá trị khác
     * @param email Địa chỉ email, null nếu không cập nhật
     * @return [ProfileResult] chứa thông tin hồ sơ đã lưu
     * @throws com.example.mobileproject.core.exception.ApiException khi dữ liệu không hợp lệ
     */
    suspend fun saveProfile(
        token: String,
        fullName: String,
        nickName: String?,
        birthDate: String,
        gender: String,
        email: String? = null,
    ): ProfileResult

    /**
     * Lấy hồ sơ cá nhân hiện tại của người dùng.
     *
     * @param token Token xác thực
     * @return [ProfileResult] chứa thông tin hồ sơ
     * @throws com.example.mobileproject.core.exception.ApiException khi token không hợp lệ
     */
    suspend fun getProfile(token: String): ProfileResult

    /**
     * Kiểm tra trạng thái ghép đôi hiện tại của người dùng.
     *
     * Trạng thái có thể là: chưa ghép đôi, đang chờ xác nhận, đã ghép đôi.
     *
     * @param token Token xác thực
     * @return [CoupleStatus] phản ánh trạng thái ghép đôi hiện tại
     */
    suspend fun getCoupleStatus(token: String): CoupleStatus

    /**
     * Lấy thông tin tóm tắt hồ sơ của đối phương (partner).
     *
     * @param token Token xác thực
     * @return [PartnerProfileSummary] chứa tên, avatar, trạng thái online của đối phương
     * @throws com.example.mobileproject.core.exception.ApiException khi chưa ghép đôi
     */
    suspend fun getPartnerProfileSummary(token: String): PartnerProfileSummary

    /**
     * Gửi yêu cầu ghép đôi đến đối phương thông qua mã cặp đôi (partner code).
     *
     * @param token Token xác thực
     * @param partnerCode Mã ghép đôi của đối phương (6 ký tự)
     * @return [CoupleRequestAction] xác nhận yêu cầu đã được gửi
     * @throws com.example.mobileproject.core.exception.ApiException khi mã không hợp lệ hoặc đã ghép đôi
     */
    suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction

    /**
     * Chấp nhận hoặc từ chối yêu cầu ghép đôi từ đối phương.
     *
     * @param token Token xác thực
     * @param requestId ID của yêu cầu ghép đôi cần xử lý
     * @param accept true để chấp nhận, false để từ chối
     * @return [CoupleRequestAction] xác nhận kết quả xử lý
     * @throws com.example.mobileproject.core.exception.ApiException khi yêu cầu không tồn tại hoặc đã xử lý
     */
    suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction

    /**
     * Tải lên ảnh đại diện (avatar) mới.
     *
     * @param token Token xác thực
     * @param imageBytes Dữ liệu ảnh dạng byte array
     * @param contentType MIME type của ảnh (ví dụ: "image/jpeg", "image/png")
     * @return URL của ảnh avatar đã tải lên
     * @throws com.example.mobileproject.core.exception.ApiException khi ảnh quá lớn hoặc định dạng không hỗ trợ
     */
    suspend fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String): String

    /**
     * Lấy danh sách khung avatar (avatar frames) có sẵn.
     *
     * @param token Token xác thực
     * @return Danh sách [AvatarFrame] mà người dùng có thể sử dụng
     */
    suspend fun getAvatarFrames(token: String): List<AvatarFrame>

    /**
     * Đặt hoặc gỡ khung avatar cho hồ sơ.
     *
     * @param token Token xác thực
     * @param frameId ID khung avatar muốn đặt, null để gỡ khung hiện tại
     * @return [ProfileResult] chứa thông tin hồ sơ đã cập nhật
     */
    suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult
}
