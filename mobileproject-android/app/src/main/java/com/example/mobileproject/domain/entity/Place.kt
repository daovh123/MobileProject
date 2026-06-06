package com.example.mobileproject.domain.entity

/**
 * Domain entity đại diện cho một địa điểm (quán ăn, quán cafe, v.v.).
 * Được sử dụng trong tính năng khám phá (Explore) để hiển thị danh sách địa điểm
 * và lên kế hoạch trải nghiệm cho cặp đôi.
 *
 * @property id Định danh duy nhất của địa điểm
 * @property name Tên địa điểm, có thể null nếu chưa được cập nhật từ nguồn dữ liệu
 * @property address Địa chỉ chi tiết (số nhà, đường)
 * @property district Quận/huyện, dùng để lọc và nhóm địa điểm theo khu vực
 * @property province Tỉnh/thành phố, dùng để phân loại địa điểm theo vùng miền
 * @property effectiveTag Nhãn phân loại hiệu quả (ví dụ: "Ăn uống", "Giải trí"), dùng cho UI badge
 * @property category Danh mục chi tiết của địa điểm (ví dụ: "Quán nhậu", "Cafe acoustic")
 * @property mealType Loại bữa ăn phù hợp (ví dụ: "Sáng", "Trưa", "Tối"), hỗ trợ gợi ý theo thời điểm
 * @property rating Điểm đánh giá trung bình từ người dùng (thang 1-5)
 * @property reviewCount Tổng số lượt đánh giá, dùng để đánh giá mức độ tin cậy của rating
 * @property openHours Giờ mở cửa dạng chuỗi (ví dụ: "08:00 - 22:00"), hiển thị cho người dùng
 * @property priceRange Khoảng giá tham khảo (ví dụ: "50.000 - 150.000 VNĐ"), giúp người dùng ước tính chi phí
 * @property imageUrl URL hình ảnh đại diện của địa điểm
 * @property googleMapsUrl Liên kết Google Maps, dùng để điều hướng người dùng đến ứng dụng bản đồ
 * @property lat Vĩ độ, dùng cho tính năng hiển thị bản đồ và tính khoảng cách
 * @property lng Kinh độ, dùng cho tính năng hiển thị bản đồ và tính khoảng cách
 */
data class Place(
    val id: String,
    val name: String?,
    val address: String?,
    val district: String?,
    val province: String?,
    val effectiveTag: String?,
    val category: String?,
    val mealType: String?,
    val rating: Double?,
    val reviewCount: Int?,
    val openHours: String?,
    val priceRange: String?,
    val imageUrl: String?,
    val googleMapsUrl: String?,
    val lat: Double?,
    val lng: Double?,
)
