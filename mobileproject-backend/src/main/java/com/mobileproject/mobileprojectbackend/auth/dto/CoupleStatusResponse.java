package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho API trạng thái ghép đôi tổng hợp.
 *
 * <p>Chứa toàn bộ thông tin về trạng thái ghép đôi của người dùng hiện tại:</p>
 * <ul>
 *   <li>Trạng thái hồ sơ và ghép đôi</li>
 *   <li>Mã ghép đôi cá nhân (nếu chưa ghép)</li>
 *   <li>Yêu cầu đến/đi đang chờ</li>
 *   <li>Thông tin CoupleInfo (coupleId, startAt, daysTogether, anniversaryTomorrow)</li>
 * </ul>
 *
 * @param success                  trạng thái thành công
 * @param message                  thông báo
 * @param profileCompleted         hồ sơ đã hoàn thành
 * @param paired                   đã ghép đôi
 * @param partnerUsername           tên đăng nhập đối tác
 * @param myCoupleCode             mã ghép đôi cá nhân (null nếu đã ghép)
 * @param myCoupleCodeExpiresAt    thời điểm hết hạn mã (null nếu đã ghép)
 * @param incomingRequestId        ID yêu cầu đến đang chờ (null nếu không có)
 * @param incomingRequesterUsername tên đăng nhập người gửi yêu cầu đến
 * @param incomingRequesterDisplayName tên hiển thị người gửi yêu cầu đến
 * @param incomingCreatedAt        thời điểm tạo yêu cầu đến
 * @param outgoingRequestId        ID yêu cầu đi gần nhất (null nếu không có)
 * @param outgoingRecipientUsername tên đăng nhập người nhận yêu cầu đi
 * @param outgoingStatus           trạng thái yêu cầu đi
 * @param outgoingUpdatedAt        thời điểm cập nhật yêu cầu đi
 * @param coupleId                 ID CoupleInfo (null nếu chưa ghép)
 * @param startAt                  thời điểm bắt đầu mối quan hệ
 * @param daysTogether             số ngày bên nhau
 * @param anniversaryTomorrow      hôm nay có phải ngày kỷ niệm không
 */
public record CoupleStatusResponse(
        boolean success,
        String message,
        boolean profileCompleted,
        boolean paired,
        String partnerUsername,
        String myCoupleCode,
        String myCoupleCodeExpiresAt,
        String incomingRequestId,
        String incomingRequesterUsername,
        String incomingRequesterDisplayName,
        String incomingCreatedAt,
        String outgoingRequestId,
        String outgoingRecipientUsername,
        String outgoingStatus,
        String outgoingUpdatedAt,
        String coupleId,
        String startAt,
        Long daysTogether,
        Boolean anniversaryTomorrow
) {
    /**
     * Tạo CoupleStatusResponse thành công với đầy đủ thông tin trạng thái.
     *
     * @return CoupleStatusResponse thành công
     */
    public static CoupleStatusResponse success(String message,
                                               boolean profileCompleted,
                                               boolean paired,
                                               String partnerUsername,
                                               String myCoupleCode,
                                               String myCoupleCodeExpiresAt,
                                               String incomingRequestId,
                                               String incomingRequesterUsername,
                                               String incomingRequesterDisplayName,
                                               String incomingCreatedAt,
                                               String outgoingRequestId,
                                               String outgoingRecipientUsername,
                                               String outgoingStatus,
                                               String outgoingUpdatedAt,
                                               String coupleId,
                                               String startAt,
                                               Long daysTogether,
                                               Boolean anniversaryTomorrow) {
        return new CoupleStatusResponse(
                true,
                message,
                profileCompleted,
                paired,
                partnerUsername,
                myCoupleCode,
                myCoupleCodeExpiresAt,
                incomingRequestId,
                incomingRequesterUsername,
                incomingRequesterDisplayName,
                incomingCreatedAt,
                outgoingRequestId,
                outgoingRecipientUsername,
                outgoingStatus,
                outgoingUpdatedAt,
                coupleId,
                startAt,
                daysTogether,
                anniversaryTomorrow
        );
    }
}
