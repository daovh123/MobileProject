package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.place.ExplorePlanRequestDto
import com.example.mobileproject.data.model.place.ExplorePlanResponseDto
import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.PlaceSearchResponseDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import javax.inject.Inject

/**
 * Wrapper trung gian cho [PlaceApiService], đóng vai trò abstraction layer.
 *
 * ## Tại sao cần wrapper này?
 * - **Giảm coupling**: Repository không phụ thuộc trực tiếp vào Retrofit interface
 * - **Dễ test**: Có thể mock [PlaceRemoteDataSource] thay vì mock [PlaceApiService]
 * - **Transform params**: Chuyển đổi nhiều tham số lẻ thành DTO request object
 *   (ví dụ: [getExplorePlan] nhận các param rời và tạo [ExplorePlanRequestDto])
 * - **Centralize logic**: Có thể thêm retry, logging, caching ở tầng này
 */
class PlaceRemoteDataSource @Inject constructor(
    private val placeApiService: PlaceApiService,
) {
    /**
     * Tìm kiếm địa điểm với nhiều bộ lọc.
     *
     * @param query từ khóa tìm kiếm (nullable)
     * @param province tỉnh/thành phố (nullable)
     * @param district quận/huyện (nullable)
     * @param type loại địa điểm (restaurant, cafe, ...)
     * @param minRating đánh giá tối thiểu (nullable)
     * @param nearLat tọa độ vĩ độ hiện tại (nullable)
     * @param nearLng tọa độ kinh độ hiện tại (nullable)
     * @param radiusKm bán kính tìm kiếm km (nullable)
     * @param page số trang (0-indexed)
     * @param size kích thước trang
     * @param sort cách sắp xếp
     * @return [PlaceSearchResponseDto] chứa danh sách địa điểm phân trang
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
    ): PlaceSearchResponseDto {
        return placeApiService.searchPlaces(
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
            page = page,
            size = size,
            sort = sort,
        )
    }

    /**
     * Lấy ngẫu nhiên một địa điểm theo bộ lọc.
     *
     * @param query từ khóa tìm kiếm (nullable)
     * @param province tỉnh/thành phố (nullable)
     * @param district quận/huyện (nullable)
     * @param type loại địa điểm
     * @param minRating đánh giá tối thiểu (nullable)
     * @param nearLat tọa độ vĩ độ (nullable)
     * @param nearLng tọa độ kinh độ (nullable)
     * @param radiusKm bán kính km (nullable)
     * @return [PlaceDto] một địa điểm ngẫu nhiên
     */
    suspend fun randomPlace(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
    ): PlaceDto {
        return placeApiService.randomPlace(
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
        )
    }

    /**
     * Tạo kế hoạch khám phá (explore plan) với nhiều tham số.
     *
     * Phương thức này nhận các tham số rời và tự tạo [ExplorePlanRequestDto]
     * để gửi lên server, giúp Repository không cần biết về DTO structure.
     *
     * @param randomSeed seed ngẫu nhiên (nullable, để tái tạo kết quả)
     * @param budget ngân sách (VND)
     * @param peopleCount số người
     * @param desiredStops số điểm dừng mong muốn
     * @param query từ khóa tìm kiếm (nullable)
     * @param province tỉnh/thành phố (nullable)
     * @param district quận/huyện (nullable)
     * @param type loại địa điểm
     * @param minRating đánh giá tối thiểu (nullable)
     * @param nearLat tọa độ vĩ độ (nullable)
     * @param nearLng tọa độ kinh độ (nullable)
     * @param radiusKm bán kính km (nullable)
     * @param excludePlaceIds danh sách ID cần loại trừ
     * @param viewedPlaceIds danh sách ID đã xem
     * @param gonePlaceIds danh sách ID đã đi
     * @param sentPlaceIds danh sách ID đã gửi
     * @param recentKeywords từ khóa tìm kiếm gần đây
     * @return [ExplorePlanResponseDto] kế hoạch khám phá
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
    ): ExplorePlanResponseDto {
        return placeApiService.getExplorePlan(
            ExplorePlanRequestDto(
                randomSeed = randomSeed,
                budget = budget,
                peopleCount = peopleCount,
                desiredStops = desiredStops,
                query = query,
                province = province,
                district = district,
                type = type,
                minRating = minRating,
                nearLat = nearLat,
                nearLng = nearLng,
                radiusKm = radiusKm,
                excludePlaceIds = excludePlaceIds,
                viewedPlaceIds = viewedPlaceIds,
                gonePlaceIds = gonePlaceIds,
                sentPlaceIds = sentPlaceIds,
                recentKeywords = recentKeywords,
            ),
        )
    }

    /**
     * Lấy các tùy chọn bộ lọc có sẵn từ server.
     *
     * @return [PlaceFilterOptionsDto] danh sách loại địa điểm, khoảng giá, ...
     */
    suspend fun getFilterOptions(): PlaceFilterOptionsDto {
        return placeApiService.getFilterOptions()
    }

    /**
     * Lấy danh sách tỉnh/thành phố Việt Nam.
     *
     * @return [List]<[VietnamProvinceDto]> danh sách tỉnh/thành phố
     */
    suspend fun getVietnamProvinces(): List<VietnamProvinceDto> {
        return placeApiService.getVietnamProvinces(depth = 1)
    }
}
