package com.example.mobileproject.data.model.onboarding

import com.google.gson.annotations.SerializedName

/**
 * Request body cho API cập nhật/tạo hồ sơ cá nhân (upsert).
 *
 * Dùng trong onboarding step đầu tiên.
 * [fullName], [birthDate], [gender] là bắt buộc.
 */
data class ProfileUpsertRequestDto(
    /** Họ và tên đầy đủ, bắt buộc. */
    @SerializedName("fullName") val fullName: String,
    /** Tên nickname hiển thị, nullable. */
    @SerializedName("nickName") val nickName: String?,
    /** Ngày sinh, định dạng ISO date (VD: "2000-01-15"), bắt buộc. */
    @SerializedName("birthDate") val birthDate: String,
    /** Giới tính (VD: "MALE", "FEMALE", "OTHER"), bắt buộc. */
    @SerializedName("gender") val gender: String,
    /** Email, nullable, có thể dùng để cập nhật email mới. */
    @SerializedName("email") val email: String? = null,
)

/**
 * Response từ API cập nhật hồ sơ cá nhân.
 *
 * Trả về thông tin hồ sơ đầy đủ sau khi cập nhật.
 * [profileCompleted] và [coupleConnected] dùng để điều hướng onboarding flow.
 */
data class ProfileResponseDto(
    /** true nếu cập nhật thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả. */
    @SerializedName("message") val message: String,
    /** Username của người dùng, nullable. */
    @SerializedName("username") val username: String?,
    /** Họ và tên đầy đủ, nullable. */
    @SerializedName("fullName") val fullName: String?,
    /** Tên nickname, nullable. */
    @SerializedName("nickName") val nickName: String?,
    /** Ngày sinh, định dạng ISO date, nullable. */
    @SerializedName("birthDate") val birthDate: String?,
    /** Giới tính, nullable. */
    @SerializedName("gender") val gender: String?,
    /** true nếu hồ sơ đã hoàn thành đầy đủ. */
    @SerializedName("profileCompleted") val profileCompleted: Boolean,
    /** true nếu đã kết nối couple với partner.
     * API có thể trả về với alternate name "paired". */
    @SerializedName(value = "coupleConnected", alternate = ["paired"]) val coupleConnected: Boolean,
    /** Email, nullable. */
    @SerializedName("email") val email: String? = null,
    /** URL avatar người dùng, nullable. */
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    /** ID frame avatar đang sử dụng, nullable. */
    @SerializedName("avatarFrameId") val avatarFrameId: String? = null,
)

/**
 * Response từ API kiểm tra trạng thái couple.
 *
 * Chứa thông tin về trạng thái kết nối couple, partner, mã mời,
 * và các request kết nối đang chờ xử lý.
 *
 * API trả về alternate name: "paired" và "coupleConnected" đều chỉ cùng một giá trị.
 */
data class CoupleStatusResponseDto(
    /** true nếu API thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả. */
    @SerializedName("message") val message: String,
    /** true nếu hồ sơ cá nhân đã hoàn thành. */
    @SerializedName("profileCompleted") val profileCompleted: Boolean,
    /** true nếu đã kết nối couple.
     * API có thể trả về với alternate name "coupleConnected". */
    @SerializedName(value = "paired", alternate = ["coupleConnected"]) val paired: Boolean,
    /** Username của partner, null nếu chưa kết nối. */
    @SerializedName("partnerUsername") val partnerUsername: String?,
    /** Mã couple code dùng để mời partner kết nối, null nếu đã kết nối. */
    @SerializedName("myCoupleCode") val myCoupleCode: String?,
    /** Thời điểm hết hạn của couple code, định dạng ISO datetime, nullable. */
    @SerializedName("myCoupleCodeExpiresAt") val myCoupleCodeExpiresAt: String? = null,
    /** UUID của couple, null nếu chưa kết nối. */
    @SerializedName("coupleId") val coupleId: String? = null,
    /** Thời điểm bắt đầu mối quan hệ, định dạng ISO datetime, nullable. */
    @SerializedName("startAt") val startAt: String? = null,
    /** Số ngày bên nhau, nullable. */
    @SerializedName("daysTogether") val daysTogether: Long? = null,
    /** true nếu ngày mai là kỷ niệm, nullable. */
    @SerializedName("anniversaryTomorrow") val anniversaryTomorrow: Boolean? = null,
    /** UUID của request kết nối đến (incoming), null nếu không có. */
    @SerializedName("incomingRequestId") val incomingRequestId: String? = null,
    /** Username của người gửi request kết nối, null nếu không có. */
    @SerializedName("incomingRequesterUsername") val incomingRequesterUsername: String? = null,
    /** Tên hiển thị của người gửi request kết nối, null nếu không có. */
    @SerializedName("incomingRequesterDisplayName") val incomingRequesterDisplayName: String? = null,
    /** Thời điểm tạo incoming request, định dạng ISO datetime, nullable. */
    @SerializedName("incomingCreatedAt") val incomingCreatedAt: String? = null,
    /** UUID của request kết nối đã gửi (outgoing), null nếu không có. */
    @SerializedName("outgoingRequestId") val outgoingRequestId: String? = null,
    /** Username của người nhận request kết nối đã gửi, null nếu không có. */
    @SerializedName("outgoingRecipientUsername") val outgoingRecipientUsername: String? = null,
    /** Trạng thái outgoing request ("PENDING", "ACCEPTED", "REJECTED"), nullable. */
    @SerializedName("outgoingStatus") val outgoingStatus: String? = null,
    /** Thời điểm cập nhật outgoing request, định dạng ISO datetime, nullable. */
    @SerializedName("outgoingUpdatedAt") val outgoingUpdatedAt: String? = null,
)

/**
 * Response từ API lấy thông tin hồ sơ partner.
 *
 * Chỉ trả về khi couple đã kết nối ([paired] = true).
 */
data class CouplePartnerProfileResponseDto(
    /** true nếu API thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả. */
    @SerializedName("message") val message: String,
    /** true nếu đã kết nối couple. */
    @SerializedName("paired") val paired: Boolean,
    /** Username của partner, nullable. */
    @SerializedName("username") val username: String?,
    /** Họ tên đầy đủ của partner, nullable. */
    @SerializedName("fullName") val fullName: String?,
    /** Nickname của partner, nullable. */
    @SerializedName("nickName") val nickName: String?,
    /** URL avatar của partner, nullable. */
    @SerializedName("avatarUrl") val avatarUrl: String?,
    /** Thời điểm bắt đầu mối quan hệ, định dạng ISO datetime, nullable. */
    @SerializedName("startAt") val startAt: String?,
    /** Số ngày bên nhau, nullable. */
    @SerializedName("daysTogether") val daysTogether: Long?,
)

/**
 * Request body cho API gửi yêu cầu kết nối couple.
 *
 * [partnerCode] là mã couple code mà partner đã tạo.
 */
data class CoupleRequestCreateRequestDto(
    /** Mã couple code của partner, bắt buộc. */
    @SerializedName("partnerCode") val partnerCode: String,
)

/**
 * Request body cho API chấp nhận/từ chối yêu cầu kết nối couple.
 */
data class CoupleRequestDecisionRequestDto(
    /** true để chấp nhận, false để từ chối. */
    @SerializedName("accept") val accept: Boolean,
)

/**
 * Response từ API xử lý yêu cầu kết nối couple (gửi, chấp nhận, từ chối).
 */
data class CoupleRequestActionResponseDto(
    /** true nếu thao tác thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả. */
    @SerializedName("message") val message: String,
    /** UUID của request kết nối, nullable. */
    @SerializedName("requestId") val requestId: String?,
    /** Trạng thái request sau thao tác, nullable. */
    @SerializedName("status") val status: String?,
    /** Username của người gửi request, nullable. */
    @SerializedName("requesterUsername") val requesterUsername: String?,
    /** Username của người nhận request, nullable. */
    @SerializedName("recipientUsername") val recipientUsername: String?,
)

/**
 * Response từ API upload avatar.
 */
data class AvatarUploadResponseDto(
    /** true nếu upload thành công. */
    @SerializedName("success") val success: Boolean,
    /** Thông báo mô tả kết quả. */
    @SerializedName("message") val message: String,
    /** URL avatar mới, null nếu upload thất bại. */
    @SerializedName("avatarUrl") val avatarUrl: String?,
)

/**
 * DTO đại diện cho một frame avatar.
 *
 * Frame avatar là viền trang trí bao quanh ảnh avatar người dùng.
 */
data class AvatarFrameDto(
    /** UUID của frame, bắt buộc. */
    @SerializedName("id") val id: String,
    /** Tên hiển thị của frame, bắt buộc. */
    @SerializedName("name") val name: String,
    /** Key tài nguyên dùng để load hình ảnh frame trên client, bắt buộc. */
    @SerializedName("resourceKey") val resourceKey: String,
    /** Mã màu sắc của frame (hex color), bắt buộc. */
    @SerializedName("color") val color: String,
)

/**
 * Request body cho API chọn/đổi frame avatar.
 *
 * [frameId] nullable — truyền null hoặc rỗng để bỏ frame.
 */
data class AvatarFrameRequestDto(
    /** UUID của frame avatar muốn sử dụng, nullable. */
    @SerializedName("frameId") val frameId: String?,
)
