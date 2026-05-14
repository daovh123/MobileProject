package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.PlaceApiService
import com.example.mobileproject.data.datasource.remote.PlaceRemoteDataSource
import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.PlaceSearchResponseDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceRepositoryImplTest {

    @Test
    fun `searchPlaces maps dto list to domain list`() = runBlocking {
        val placeApiService = object : PlaceApiService {
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
            ): PlaceSearchResponseDto {
                return PlaceSearchResponseDto(
                    items = listOf(
                        PlaceDto(
                            id = "p1",
                            name = "Pho Ong Cat",
                            address = "201 Pham Ngu Lao",
                            district = "Quan 1",
                            province = "Thanh pho Ho Chi Minh",
                            effectiveTag = "Bun / Pho",
                            category = null,
                            mealType = "Bua toi",
                            rating = 4.6,
                            reviewCount = 5002,
                            openHours = "07:00 - 22:00",
                            priceRange = "100000-200000",
                            imageUrl = null,
                            googleMapsUrl = null,
                            lat = 10.77,
                            lng = 106.69,
                        ),
                        PlaceDto(
                            id = "p2",
                            name = "Coffee Date",
                            address = "So 1 Chu Manh Trinh",
                            district = "Quan 1",
                            province = "Thanh pho Ho Chi Minh",
                            effectiveTag = null,
                            category = "Quan ca phe",
                            mealType = null,
                            rating = null,
                            reviewCount = null,
                            openHours = null,
                            priceRange = null,
                            imageUrl = null,
                            googleMapsUrl = null,
                            lat = null,
                            lng = null,
                        ),
                    ),
                    total = 2,
                    page = 0,
                    size = 20,
                )
            }

            override suspend fun randomPlace(
                query: String?,
                province: String?,
                district: String?,
                type: String,
                minRating: Double?,
                nearLat: Double?,
                nearLng: Double?,
                radiusKm: Double?,
            ): PlaceDto {
                return PlaceDto(
                    id = "p1",
                    name = "Pho Ong Cat",
                    address = "201 Pham Ngu Lao",
                    district = "Quan 1",
                    province = "Thanh pho Ho Chi Minh",
                    effectiveTag = "Bun / Pho",
                    category = null,
                    mealType = "Bua toi",
                    rating = 4.6,
                    reviewCount = 5002,
                    openHours = "07:00 - 22:00",
                    priceRange = "100000-200000",
                    imageUrl = null,
                    googleMapsUrl = null,
                    lat = 10.77,
                    lng = 106.69,
                )
            }

            override suspend fun getFilterOptions(): PlaceFilterOptionsDto {
                return PlaceFilterOptionsDto(
                    districts = listOf("Q. 1", "Q. 3"),
                    provinces = listOf("TP. Ho Chi Minh"),
                )
            }

            override suspend fun getVietnamProvinces(depth: Int): List<VietnamProvinceDto> {
                return listOf(
                    VietnamProvinceDto(code = 1, name = "Ha Noi"),
                    VietnamProvinceDto(code = 79, name = "Thanh pho Ho Chi Minh"),
                )
            }
        }

        val repository = PlaceRepositoryImpl(
            placeRemoteDataSource = PlaceRemoteDataSource(placeApiService),
        )

        val result = repository.searchPlaces(
            query = "pho",
            province = "Thanh pho Ho Chi Minh",
            district = "Q. 1",
            type = "food",
            minRating = 4.0,
            nearLat = null,
            nearLng = null,
            radiusKm = null,
            page = 0,
            size = 20,
            sort = "trending",
        )

        assertEquals(2, result.total)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertEquals(2, result.items.size)
        assertEquals("p1", result.items[0].id)
        assertEquals("Pho Ong Cat", result.items[0].name)
        assertEquals("Quan ca phe", result.items[1].effectiveTag)
        assertEquals(null, result.items[1].rating)
    }
}