package com.example.mobileproject.data.model.place

import com.google.gson.annotations.SerializedName

/**
 * Response phân trang cho API tìm kiếm địa điểm.
 *
 * Hỗ trợ phân trang với [page], [size], [total].
 * [items] có thể rỗng nếu không tìm thấy kết quả.
 */
data class PlaceSearchResponseDto(
    /** Danh sách địa điểm ở trang hiện tại, mặc định rỗng. */
    @SerializedName("items")
    val items: List<PlaceDto> = emptyList(),
    /** Tổng số địa điểm khớp tìm kiếm trên tất cả các trang. */
    @SerializedName("total")
    val total: Long = 0,
    /** Số trang hiện tại (0-indexed). */
    @SerializedName("page")
    val page: Int = 0,
    /** Số phần tử trên mỗi trang. */
    @SerializedName("size")
    val size: Int = 0,
)

/**
 * DTO đại diện cho một địa điểm (nhà hàng, quán cafe, v.v.).
 *
 * Hầu hết các trường nullable vì dữ liệu từ nhiều nguồn khác nhau,
 * một số trường có thể không có sẵn.
 */
data class PlaceDto(
    /** UUID định danh duy nhất của địa điểm, bắt buộc. */
    @SerializedName("id")
    val id: String,
    /** Tên hiển thị của địa điểm, nullable. */
    @SerializedName("name")
    val name: String?,
    /** Địa chỉ chi tiết, nullable. */
    @SerializedName("address")
    val address: String?,
    /** Quận/huyện, nullable. */
    @SerializedName("district")
    val district: String?,
    /** Tỉnh/thành phố, nullable. */
    @SerializedName("province")
    val province: String?,
    /** Tag hiệu quả mô tả loại trải nghiệm (VD: "Ăn tối", "Cafe"), nullable.
     * Ưu tiên dùng field này, fallback về [category] nếu null. */
    @SerializedName("effectiveTag")
    val effectiveTag: String?,
    /** Phân loại danh mục (VD: "restaurant", "cafe"), nullable. */
    @SerializedName("category")
    val category: String?,
    /** Loại bữa ăn (VD: "breakfast", "lunch", "dinner"), nullable. */
    @SerializedName("mealType")
    val mealType: String?,
    /** Điểm đánh giá trung bình (thang 0-5), nullable. */
    @SerializedName("rating")
    val rating: Double?,
    /** Tổng số lượt đánh giá, nullable. */
    @SerializedName("reviewCount")
    val reviewCount: Int?,
    /** Giờ mở cửa, định dạng chuỗi (VD: "08:00 - 22:00"), nullable. */
    @SerializedName("openHours")
    val openHours: String?,
    /** Khoảng giá, định dạng chuỗi (VD: "50000-150000"), nullable. */
    @SerializedName("priceRange")
    val priceRange: String?,
    /** URL hình ảnh đại diện, nullable. */
    @SerializedName("imageUrl")
    val imageUrl: String?,
    /** URL dẫn đến Google Maps, nullable. */
    @SerializedName("googleMapsUrl")
    val googleMapsUrl: String?,
    /** Vĩ độ (latitude), nullable. */
    @SerializedName("lat")
    val lat: Double?,
    /** Kinh độ (longitude), nullable. */
    @SerializedName("lng")
    val lng: Double?,
)

/**
 * Request body cho API tạo kế hoạch khám phá (explore plan).
 *
 * Hỗ trợ lọc theo ngân sách, số người, vị trí, đánh giá tối thiểu,
 * và loại trừ các địa điểm đã xem/đã đi.
 */
data class ExplorePlanRequestDto(
    /** Seed ngẫu nhiên để tái tạo kết quả, null = random mỗi lần. */
    @SerializedName("randomSeed")
    val randomSeed: Long? = null,
    /** Ngân sách tối đa (VNĐ), bắt buộc. */
    @SerializedName("budget")
    val budget: Long,
    /** Số người tham gia, bắt buộc, tối thiểu 1. */
    @SerializedName("peopleCount")
    val peopleCount: Int,
    /** Số điểm dừng mong muốn, bắt buộc, tối thiểu 1. */
    @SerializedName("desiredStops")
    val desiredStops: Int,
    /** Từ khóa tìm kiếm tự do, nullable. */
    @SerializedName("q")
    val query: String?,
    /** Lọc theo tỉnh/thành phố, nullable. */
    @SerializedName("province")
    val province: String?,
    /** Lọc theo quận/huyện, nullable. */
    @SerializedName("district")
    val district: String?,
    /** Loại trải nghiệm (VD: "food", "drink"), bắt buộc. */
    @SerializedName("type")
    val type: String,
    /** Điểm đánh giá tối thiểu, nullable. */
    @SerializedName("minRating")
    val minRating: Double?,
    /** Vĩ độ vị trí hiện tại để ưu tiên kết quả gần, nullable. */
    @SerializedName("nearLat")
    val nearLat: Double?,
    /** Kinh độ vị trí hiện tại, nullable. */
    @SerializedName("nearLng")
    val nearLng: Double?,
    /** Bán kính tìm kiếm (km), nullable. */
    @SerializedName("radiusKm")
    val radiusKm: Double?,
    /** Danh sách ID địa điểm cần loại trừ, mặc định rỗng. */
    @SerializedName("excludePlaceIds")
    val excludePlaceIds: List<String> = emptyList(),
    /** Danh sách ID địa điểm đã xem, mặc định rỗng. */
    @SerializedName("viewedPlaceIds")
    val viewedPlaceIds: List<String> = emptyList(),
    /** Danh sách ID địa điểm đã đi, mặc định rỗng. */
    @SerializedName("gonePlaceIds")
    val gonePlaceIds: List<String> = emptyList(),
    /** Danh sách ID địa điểm đã gửi cho partner, mặc định rỗng. */
    @SerializedName("sentPlaceIds")
    val sentPlaceIds: List<String> = emptyList(),
    /** Từ khóa tìm kiếm gần đây, mặc định rỗng. */
    @SerializedName("recentKeywords")
    val recentKeywords: List<String> = emptyList(),
)

/**
 * Response từ API tạo kế hoạch khám phá.
 *
 * Chứa [summary] tổng quan kế hoạch và [items] danh sách các điểm dừng.
 */
data class ExplorePlanResponseDto(
    /** Thông tin tổng quan về kế hoạch (ngân sách, số người, v.v.). */
    @SerializedName("summary")
    val summary: ExplorePlanSummaryDto,
    /** Danh sách các điểm dừng trong kế hoạch, mặc định rỗng. */
    @SerializedName("items")
    val items: List<ExplorePlanItemDto> = emptyList(),
)

/**
 * DTO tổng quan kế hoạch khám phá.
 *
 * Bao gồm thông tin ngân sách, chi phí ước tính, và cảnh báo số dư thấp.
 */
data class ExplorePlanSummaryDto(
    /** Tổng ngân sách người dùng đặt (VNĐ). */
    @SerializedName("totalBudget")
    val totalBudget: Long = 0L,
    /** Số người tham gia. */
    @SerializedName("peopleCount")
    val peopleCount: Int = 0,
    /** Số điểm dừng mong muốn. */
    @SerializedName("desiredStops")
    val desiredStops: Int = 0,
    /** Tổng chi phí ước tính (VNĐ), có thể vượt [totalBudget]. */
    @SerializedName("estimatedTotalCost")
    val estimatedTotalCost: Long = 0L,
    /** true nếu số dư ví không đủ chi trả [estimatedTotalCost]. */
    @SerializedName("lowBalance")
    val lowBalance: Boolean = false,
    /** Thông báo cảnh báo khi số dư thấp, nullable. */
    @SerializedName("balanceMessage")
    val balanceMessage: String? = null,
    /** Ngân sách đề xuất mặc định cho lần sau (VNĐ). */
    @SerializedName("suggestedDefaultBudget")
    val suggestedDefaultBudget: Long = 0L,
)

/**
 * DTO cho một điểm dừng trong kế hoạch khám phá.
 *
 * Mỗi item bao gồm thông tin địa điểm, loại trải nghiệm và chi phí ước tính.
 */
data class ExplorePlanItemDto(
    /** Thứ tự của điểm dừng trong kế hoạch (1-indexed). */
    @SerializedName("stopOrder")
    val stopOrder: Int,
    /** Loại trải nghiệm (VD: "Ăn trưa", "Cafe", "Dạo chơi"). */
    @SerializedName("experienceType")
    val experienceType: String,
    /** Chi phí ước tính cho điểm dừng này (VNĐ). */
    @SerializedName("estimatedCost")
    val estimatedCost: Long,
    /** Lý do gợi ý địa điểm này. */
    @SerializedName("reason")
    val reason: String,
    /** Thông tin chi tiết địa điểm. */
    @SerializedName("place")
    val place: PlaceDto,
)

/**
 * DTO chứa các tùy chọn lọc cho tìm kiếm địa điểm.
 *
 * Dùng để populate dropdown/filter UI.
 */
data class PlaceFilterOptionsDto(
    /** Danh sách quận/huyện có sẵn, mặc định rỗng. */
    @SerializedName("districts")
    val districts: List<String> = emptyList(),
    /** Danh sách tỉnh/thành phố có sẵn, mặc định rỗng. */
    @SerializedName("provinces")
    val provinces: List<String> = emptyList(),
)

/**
 * DTO đại diện cho một tỉnh/thành phố của Việt Nam.
 *
 * Dùng để ánh xạ danh sách tỉnh từ API công khai.
 */
data class VietnamProvinceDto(
    /** Mã tỉnh theo chuẩn hành chính Việt Nam. */
    @SerializedName("code")
    val code: Int,
    /** Tên tỉnh/thành phố, nullable. */
    @SerializedName("name")
    val name: String?,
)
