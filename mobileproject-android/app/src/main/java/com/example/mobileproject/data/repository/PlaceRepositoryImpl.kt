package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.PlaceRemoteDataSource
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.mapper.toProvinceNames
import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.PlaceSearchPage
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * Implementation của [PlaceRepository], xử lý tìm kiếm và khám phá địa điểm.
 *
 * ## Caching strategy
 * Không có cache local, tất cả dữ liệu lấy từ remote API.
 * Phù hợp vì dữ liệu địa điểm thay đổi theo vị trí và thời gian thực.
 *
 * ## Data transformation
 * Chuyển đổi DTO -> domain entity qua mapper extension:
 * - `PlaceDto.toDomain()` -> [Place]
 * - `PlaceSearchResponseDto` -> [PlaceSearchPage]
 * - `ExplorePlanResponseDto.toDomain()` -> [ExplorePlan]
 * - `List<VietnamProvinceDto>.toProvinceNames()` -> `List<String>`
 *
 * ## Threading
 * Tất cả hàm `suspend` chạy trên IO dispatcher (Retrofit default).
 */
class PlaceRepositoryImpl @Inject constructor(
    private val placeRemoteDataSource: PlaceRemoteDataSource,
) : PlaceRepository {

    /**
     * Tìm kiếm địa điểm với phân trang và bộ lọc.
     *
     * @return [PlaceSearchPage] chứa danh sách địa điểm và metadata phân trang
     */
    override suspend fun searchPlaces(
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
    ): PlaceSearchPage {
        val response = placeRemoteDataSource.searchPlaces(
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

        return PlaceSearchPage(
            items = response.items.map { it.toDomain() },
            total = response.total,
            page = response.page,
            size = response.size,
        )
    }

    /**
     * Lấy ngẫu nhiên một địa điểm theo bộ lọc (tính năng "Đi đâu?").
     *
     * @return [Place] một địa điểm ngẫu nhiên
     */
    override suspend fun getRandomPlace(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
    ): Place {
        return placeRemoteDataSource.randomPlace(
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
        ).toDomain()
    }

    /**
     * Tạo kế hoạch khám phá theo ngân sách và bộ lọc.
     *
     * @return [ExplorePlan] chứa danh sách gợi ý địa điểm
     */
    override suspend fun getExplorePlan(
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
    ): ExplorePlan {
        return placeRemoteDataSource.getExplorePlan(
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
        ).toDomain()
    }

    /**
     * Lấy danh sách tùy chọn bộ lọc (loại địa điểm, khoảng giá, ...).
     *
     * @return [PlaceFilterOptions]
     */
    override suspend fun getFilterOptions(): PlaceFilterOptions {
        return placeRemoteDataSource.getFilterOptions().toDomain()
    }

    /**
     * Lấy danh sách tên tỉnh/thành phố Việt Nam.
     *
     * @return `List<String>` danh sách tên tỉnh/thành phố
     */
    override suspend fun getVietnamProvinces(): List<String> {
        return placeRemoteDataSource.getVietnamProvinces().toProvinceNames()
    }
}
