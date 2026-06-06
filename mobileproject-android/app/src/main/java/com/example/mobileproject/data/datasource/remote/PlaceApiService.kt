package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.place.ExplorePlanRequestDto
import com.example.mobileproject.data.model.place.ExplorePlanResponseDto
import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.PlaceSearchResponseDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API service cho các endpoint liên quan đến địa điểm (Place/Explore).
 *
 * ## Đặc điểm
 * - Không yêu cầu authentication (public endpoints)
 * - Sử dụng base URL riêng (khác với [ApiService])
 * - Endpoint provinces sử dụng API bên thứ ba: `provinces.open-api.vn`
 */
interface PlaceApiService {

    /**
     * Tìm kiếm địa điểm với nhiều bộ lọc.
     *
     * - **HTTP**: `GET api/places`
     * - **Auth**: Không yêu cầu
     * - **Query params**:
     *   - `q` - từ khóa tìm kiếm (nullable)
     *   - `province` - tỉnh/thành phố (nullable)
     *   - `district` - quận/huyện (nullable)
     *   - `type` - loại địa điểm (restaurant, cafe, ...)
     *   - `minRating` - đánh giá tối thiểu (nullable)
     *   - `nearLat`, `nearLng` - tọa độ vị trí hiện tại (nullable)
     *   - `radiusKm` - bán kính tìm kiếm km (nullable)
     *   - `page`, `size` - phân trang
     *   - `sort` - cách sắp xếp (rating, distance, ...)
     * - **Response**: [PlaceSearchResponseDto] (items, total, page, size)
     * - **Mục đích**: Tìm kiếm và lọc địa điểm, hỗ trợ phân trang và sắp xếp
     */
    @GET("api/places")
    suspend fun searchPlaces(
        @Query("q") query: String?,
        @Query("province") province: String?,
        @Query("district") district: String?,
        @Query("type") type: String,
        @Query("minRating") minRating: Double?,
        @Query("nearLat") nearLat: Double?,
        @Query("nearLng") nearLng: Double?,
        @Query("radiusKm") radiusKm: Double?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): PlaceSearchResponseDto

    /**
     * Lấy ngẫu nhiên một địa điểm theo bộ lọc.
     *
     * - **HTTP**: `GET api/places/random`
     * - **Auth**: Không yêu cầu
     * - **Query params**: tương tự [searchPlaces] nhưng không phân trang
     * - **Response**: [PlaceDto]
     * - **Mục đích**: Tính năng "Đi đâu?" - gợi ý ngẫu nhiên một địa điểm
     */
    @GET("api/places/random")
    suspend fun randomPlace(
        @Query("q") query: String?,
        @Query("province") province: String?,
        @Query("district") district: String?,
        @Query("type") type: String,
        @Query("minRating") minRating: Double?,
        @Query("nearLat") nearLat: Double?,
        @Query("nearLng") nearLng: Double?,
        @Query("radiusKm") radiusKm: Double?,
    ): PlaceDto

    /**
     * Tạo kế hoạch khám phá (explore plan) với nhiều điểm dừng.
     *
     * - **HTTP**: `POST api/places/explore-plan`
     * - **Auth**: Không yêu cầu
     * - **Request body**: [ExplorePlanRequestDto] chứa ngân sách, số người, số điểm dừng,
     *   bộ lọc, và danh sách ID đã xem/đã đi/đã gửi
     * - **Response**: [ExplorePlanResponseDto] chứa danh sách gợi ý
     * - **Mục đích**: Tạo kế hoạch hẹn hò/khám phá tự động theo ngân sách
     */
    @POST("api/places/explore-plan")
    suspend fun getExplorePlan(
        @Body request: ExplorePlanRequestDto,
    ): ExplorePlanResponseDto

    /**
     * Lấy các tùy chọn bộ lọc có sẵn (loại địa điểm, khoảng giá, ...).
     *
     * - **HTTP**: `GET api/places/filter-options`
     * - **Auth**: Không yêu cầu
     * - **Response**: [PlaceFilterOptionsDto]
     * - **Mục đích**: Populate bộ lọc trên UI (dropdown, chip, ...)
     */
    @GET("api/places/filter-options")
    suspend fun getFilterOptions(): PlaceFilterOptionsDto

    /**
     * Lấy danh sách tỉnh/thành phố Việt Nam từ API bên thứ ba.
     *
     * - **HTTP**: `GET https://provinces.open-api.vn/api/v2/p/`
     * - **Auth**: Không yêu cầu
     * - **Query param**: `depth` - độ sâu dữ liệu (mặc định 1)
     * - **Response**: [List]<[VietnamProvinceDto]>
     * - **Mục đích**: Populate danh sách tỉnh/thành phố cho bộ lọc địa điểm
     */
    @GET("https://provinces.open-api.vn/api/v2/p/")
    suspend fun getVietnamProvinces(
        @Query("depth") depth: Int = 1,
    ): List<VietnamProvinceDto>
}
