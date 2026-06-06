package com.example.mobileproject.domain.entity

/**
 * Domain entity chứa thông tin hồ sơ cá nhân của người dùng.
 * Được sử dụng trong quy trình onboarding và trang hồ sơ cá nhân.
 *
 * @property username Tên đăng nhập, định danh chính của người dùng trong hệ thống
 * @property fullName Họ và tên đầy đủ
 * @property nickName Biệt danh, dùng để hiển thị thân mật trong ứng dụng couple
 * @property birthDate Ngày sinh dạng chuỗi (ISO format), dùng cho tính năng sinh nhật/kỷ niệm
 * @property gender Giới tính, dùng cho avatar và nội dung cá nhân hoá
 * @property profileCompleted True nếu hồ sơ đã đủ thông tin bắt buộc, điều hướng onboarding
 * @property coupleConnected True nếu đã kết nối với đối tác
 * @property email Địa chỉ email, có thể khác với email đăng ký nếu người dùng cập nhật
 * @property avatarUrl URL ảnh đại diện, null nếu chưa upload
 * @property avatarFrameId Định danh khung viền avatar, dùng cho tính năng trang trí avatar
 */
data class ProfileResult(
    val username: String?,
    val fullName: String?,
    val nickName: String?,
    val birthDate: String?,
    val gender: String?,
    val profileCompleted: Boolean,
    val coupleConnected: Boolean,
    val email: String? = null,
    val avatarUrl: String? = null,
    val avatarFrameId: String? = null,
)

/**
 * Domain entity đại diện cho khung viền avatar (avatar frame).
 * Đây là tính năng trang trí cho phép người dùng tuỳ chỉnh avatar.
 *
 * @property id Định danh duy nhất của khung viền
 * @property name Tên hiển thị của khung viền (ví dụ: "Trái tim", "Hoa hồng")
 * @property resourceKey Key để ánh xạ tới resource drawable/asset trong app
 * @property color Màu sắc chủ đạo của khung viền dạng hex string, dùng cho preview UI
 */
data class AvatarFrame(
    val id: String,
    val name: String,
    val resourceKey: String,
    val color: String,
)

/**
 * Domain entity đại diện cho một hành động trên yêu cầu kết nối couple.
 * Được sử dụng khi người dùng chấp nhận hoặc từ chối lời mời kết nối.
 *
 * @property requestId Định danh của yêu cầu kết nối, null nếu không có yêu cầu nào
 * @property status Trạng thái mới của yêu cầu sau hành động (ví dụ: "ACCEPTED", "REJECTED")
 * @property message Thông điệp phản hồi cho hành động (thành công/thất bại)
 * @property requesterUsername Tên người gửi lời mời
 * @property recipientUsername Tên người nhận lời mời
 */
data class CoupleRequestAction(
    val requestId: String?,
    val status: String?,
    val message: String,
    val requesterUsername: String?,
    val recipientUsername: String?,
)

/**
 * Domain entity chứa trạng thái tổng thể của mối quan hệ couple.
 * Đây là entity phức tạp, kết hợp thông tin về couple đã kết nối,
 * yêu cầu đang chờ, và mã mời kết nối.
 *
 * @property profileCompleted True nếu hồ sơ đã hoàn thiện
 * @property paired True nếu đã ghép đôi thành công với đối tác
 * @property partnerUsername Tên đối tác đã ghép đôi, null nếu chưa paired
 * @property myCoupleCode Mã mời kết nối của người dùng hiện tại, dùng để chia sẻ cho đối tác
 * @property myCoupleCodeExpiresAt Thời hạn hết hạn của mã mời, null nếu mã vĩnh viễn
 * @property incomingRequestId Định danh yêu cầu kết nối đến (người khác gửi cho mình)
 * @property incomingRequesterUsername Tên người gửi yêu cầu kết nối đến
 * @property incomingRequesterDisplayName Tên hiển thị của người gửi yêu cầu
 * @property incomingCreatedAt Thời điểm nhận được yêu cầu kết nối
 * @property outgoingRequestId Định danh yêu cầu kết nối đi (mình gửi cho người khác)
 * @property outgoingRecipientUsername Tên người nhận yêu cầu kết nối đi
 * @property outgoingStatus Trạng thái của yêu cầu kết nối đi (PENDING, ACCEPTED, REJECTED)
 * @property outgoingUpdatedAt Thời điểm cập nhật cuối của yêu cầu kết nối đi
 * @property coupleId Định danh cặp đôi, chỉ có giá trị khi đã paired
 * @property startAt Ngày bắt đầu mối quan hệ, dùng để tính ngày bên nhau
 * @property daysTogether Số ngày đã bên nhau, tính từ startAt
 * @property anniversaryTomorrow True nếu ngày mai là kỷ niệm (tròn số ngày/tháng bên nhau), dùng cho notification
 */
data class CoupleStatus(
    val profileCompleted: Boolean,
    val paired: Boolean,
    val partnerUsername: String?,
    val myCoupleCode: String?,
    val myCoupleCodeExpiresAt: String? = null,
    val incomingRequestId: String?,
    val incomingRequesterUsername: String?,
    val incomingRequesterDisplayName: String?,
    val incomingCreatedAt: String?,
    val outgoingRequestId: String?,
    val outgoingRecipientUsername: String?,
    val outgoingStatus: String?,
    val outgoingUpdatedAt: String?,
    val coupleId: String? = null,
    val startAt: String? = null,
    val daysTogether: Long? = null,
    val anniversaryTomorrow: Boolean? = null,
)

/**
 * Domain entity chứa thông tin tóm tắt hồ sơ của đối tác (partner).
 * Được hiển thị trên trang hồ sơ couple để người dùng xem thông tin người yêu.
 *
 * @property paired True nếu đã ghép đôi thành công
 * @property message Thông điệp trạng thái (ví dụ: "Bạn chưa có couple")
 * @property username Tên đăng nhập của đối tác
 * @property fullName Họ tên đầy đủ của đối tác
 * @property nickName Biệt danh của đối tác
 * @property avatarUrl URL avatar của đối tác
 * @property startAt Ngày bắt đầu mối quan hệ
 * @property daysTogether Số ngày đã bên nhau
 */
data class PartnerProfileSummary(
    val paired: Boolean,
    val message: String,
    val username: String?,
    val fullName: String?,
    val nickName: String?,
    val avatarUrl: String?,
    val startAt: String?,
    val daysTogether: Long?,
)
