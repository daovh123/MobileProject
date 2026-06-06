package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.PlaceSearchPage

/**
 * Repository quản lý địa điểm (places) — tìm kiếm, khám phá, và lọc.
 *
 * Cung cấp dữ liệu địa điểm cho các tính năng: tìm kiếm quán ăn/đi chơi,
 * gợi ý ngẫu nhiên, lập kế hoạch khám phá (explore plan), và lấy tùy chọn lọc.
 * Hỗ trợ lọc theo vị trí địa lý (lat/lng + bán kính), tỉnh/quận, loại địa điểm.
 */
interface PlaceRepository {

    /**
     * Tìm kiếm địa điểm với nhiều tiêu chí lọc và phân trang.
     *
     * @param query Từ khóa tìm kiếm (tên địa điểm, mô tả), null nếu không lọc theo từ khóa
     * @param province Tỉnh/thành phố, null nếu không lọc theo tỉnh
     * @param district Quận/huyện, null nếu không lọc theo quận
     * @param type Loại địa điểm (ví dụ: "restaurant", "cafe", "all")
     * @param minRating Đánh giá tối thiểu (0.0 - 5.0), null nếu không lọc theo đánh giá
     * @param nearLat Vĩ độ vị trí trung tâm để tìm kiếm lân cận
     * @param nearLng Kinh độ vị trí trung tâm để tìm kiếm lân cận
     * @param radiusKm Bán kính tìm kiếm (km), chỉ có hiệu lực khi cả nearLat và nearLng đều khác null
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng kết quả mỗi trang
     * @param sort Tiêu chí sắp xếp (ví dụ: "trending", "rating", "distance")
     * @return [PlaceSearchPage] chứa danh sách địa điểm và thông tin phân trang
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun searchPlaces(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
        page: Int,
        size: Int,
        sort: String,
    ): PlaceSearchPage

    /**
     * Lấy một địa điểm ngẫu nhiên phù hợp với tiêu chí lọc.
     *
     * Dùng cho tính năng "Đi đâu?" — gợi ý nhanh một địa điểm
     * khi cặp đôi không biết đi đâu.
     *
     * @param query Từ khóa tìm kiếm, null nếu không lọc
     * @param province Tỉnh/thành phố, null nếu không lọc
     * @param district Quận/huyện, null nếu không lọc
     * @param type Loại địa điểm (ví dụ: "restaurant", "all")
     * @param minRating Đánh giá tối thiểu (0.0 - 5.0), null nếu không lọc
     * @param nearLat Vĩ độ vị trí hiện tại
     * @param nearLng Kinh độ vị trí hiện tại
     * @param radiusKm Bán kính tìm kiếm (km)
     * @return Một [Place] ngẫu nhiên phù hợp
     * @throws NoSuchElementException nếu không tìm thấy địa điểm nào phù hợp
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun getRandomPlace(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
    ): Place

    /**
     * Lập kế hoạch khám phá (explore plan) — gợi ý nhiều địa điểm
     * trong một chuyến đi dựa trên ngân sách và số điểm dừng mong muốn.
     *
     * Tính năng này giúp cặp đôi lên lịch trình đi chơi tự động,
     * tối ưu theo ngân sách và tránh trùng lặp với các địa điểm đã xem/đã đi.
     *
     * @param randomSeed Seed ngẫu nhiên để đảm bảo kết quả khác nhau giữa các lần gọi, null dùng seed mặc định
     * @param budget Ngân sách dự kiến cho chuyến đi (đơn vị: VND)
     * @param peopleCount Số người tham gia chuyến đi
     * @param desiredStops Số điểm dừng mong muốn trong lịch trình
     * @param query Từ khóa tìm kiếm, null nếu không lọc
     * @param province Tỉnh/thành phố, null nếu không lọc
     * @param district Quận/huyện, null nếu không lọc
     * @param type Loại địa điểm, "all" nếu không lọc
     * @param minRating Đánh giá tối thiểu, null nếu không lọc
     * @param nearLat Vĩ độ vị trí xuất phát
     * @param nearLng Kinh độ vị trí xuất phát
     * @param radiusKm Bán kính tìm kiếm (km)
     * @param excludePlaceIds Danh sách ID địa điểm cần loại trừ (đã bị bỏ qua)
     * @param viewedPlaceIds Danh sách ID địa điểm đã xem nhưng chưa quyết định
     * @param gonePlaceIds Danh sách ID địa điểm đã đi rồi
     * @param sentPlaceIds Danh sách ID địa điểm đã gửi cho đối phương
     * @param recentKeywords Từ khóa tìm kiếm gần đây để cá nhân hóa gợi ý
     * @return [ExplorePlan] chứa lịch trình và danh sách địa điểm gợi ý
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun getExplorePlan(
        randomSeed: Long?,
        budget: Long,
        peopleCount: Int,
        desiredStops: Int,
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
        excludePlaceIds: List<String>,
        viewedPlaceIds: List<String>,
        gonePlaceIds: List<String>,
        sentPlaceIds: List<String>,
        recentKeywords: List<String>,
    ): ExplorePlan

    /**
     * Lấy danh sách các tùy chọn lọc (tỉnh, quận, loại địa điểm, v.v.)
     * để hiển thị trên giao diện bộ lọc.
     *
     * @return [PlaceFilterOptions] chứa các tùy chọn lọc có sẵn
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun getFilterOptions(): PlaceFilterOptions

    /**
     * Lấy danh sách tất cả tỉnh/thành phố Việt Nam.
     *
     * @return Danh sách tên tỉnh/thành phố dạng chuỗi
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun getVietnamProvinces(): List<String>
}
