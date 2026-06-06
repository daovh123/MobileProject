package com.example.mobileproject.domain.entity

/**
 * Domain entity đại diện cho kế hoạch khám phá (Explore Plan) của cặp đôi.
 * Chứa thông tin tổng quan và danh sách các điểm dừng chân được gợi ý
 * dựa trên ngân sách và sở thích của người dùng.
 *
 * @property summary Thông tin tổng quan về kế hoạch (ngân sách, số người, ước tính chi phí)
 * @property items Danh sách các điểm dừng chân theo thứ tự đề xuất
 */
data class ExplorePlan(
    val summary: ExplorePlanSummary,
    val items: List<ExplorePlanItem>,
)

/**
 * Thông tin tổng quan của kế hoạch khám phá, cung cấp cái nhìn nhanh
 * về ngân sách và khả năng chi trả cho cả chuyến đi.
 *
 * @property totalBudget Tổng ngân sách mà cặp đôi dự định chi cho chuyến đi (đơn vị: VNĐ)
 * @property peopleCount Số người tham gia, ảnh hưởng đến ước tính chi phí
 * @property desiredStops Số điểm dừng chân mà người dùng mong muốn
 * @property estimatedTotalCost Tổng chi phí ước tính cho toàn bộ kế hoạch
 * @property lowBalance True nếu số dư ví hiện tại thấp hơn chi phí ước tính, cảnh báo người dùng nạp thêm tiền
 * @property balanceMessage Thông điệp cảnh báo khi số dư không đủ, null nếu đủ ngân sách
 * @property suggestedDefaultBudget Ngân sách đề xuất mặc định, dựa trên phân tích dữ liệu chi tiêu trước đó
 */
data class ExplorePlanSummary(
    val totalBudget: Long,
    val peopleCount: Int,
    val desiredStops: Int,
    val estimatedTotalCost: Long,
    val lowBalance: Boolean,
    val balanceMessage: String?,
    val suggestedDefaultBudget: Long,
)

/**
 * Một điểm dừng chân trong kế hoạch khám phá.
 * Mỗi item đại diện cho một trải nghiệm được gợi ý tại một địa điểm cụ thể.
 *
 * @property stopOrder Thứ tự dừng chân trong chuyến đi (bắt đầu từ 1)
 * @property experienceType Loại trải nghiệm (ví dụ: "Ăn trưa", "Check-in cafe", "Dạo phố")
 * @property estimatedCost Chi phí ước tính cho điểm dừng này (đơn vị: VNĐ)
 * @property reason Lý do gợi ý địa điểm này, giúp người dùng hiểu tại sao nó phù hợp
 * @property place Thông tin chi tiết của địa điểm được gợi ý
 */
data class ExplorePlanItem(
    val stopOrder: Int,
    val experienceType: String,
    val estimatedCost: Long,
    val reason: String,
    val place: Place,
)
