package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.PlaceSearchPage
import com.example.mobileproject.domain.repository.PlaceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchPlacesUseCaseTest {

    @Test
    fun `invoke delegates search params to repository`() = runBlocking {
        val expected = listOf(
            Place(
                id = "p1",
                name = "Pho Ong Cat",
                address = "201 Pham Ngu Lao",
                district = "Quan 1",
                province = "Thanh pho Ho Chi Minh",
                effectiveTag = "Bun / Pho",
                category = "Quan an",
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
        )

        var capturedQuery: String? = null
        var capturedProvince: String? = null
        var capturedDistrict: String? = null
        var capturedType = ""
        var capturedMinRating: Double? = null
        var capturedPage = -1
        var capturedSize = -1
        var capturedSort = ""

        val repository = object : PlaceRepository {
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
                capturedQuery = query
                capturedProvince = province
                capturedDistrict = district
                capturedType = type
                capturedMinRating = minRating
                capturedPage = page
                capturedSize = size
                capturedSort = sort
                return PlaceSearchPage(
                    items = expected,
                    total = expected.size.toLong(),
                    page = page,
                    size = size,
                )
            }

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
                return expected.first()
            }

            override suspend fun getFilterOptions(): PlaceFilterOptions {
                return PlaceFilterOptions()
            }

            override suspend fun getVietnamProvinces(): List<String> {
                return emptyList()
            }
        }

        val useCase = SearchPlacesUseCase(repository)
        val result = useCase(
            query = "pho",
            province = "TP. Ho Chi Minh",
            district = "Q. 1",
            type = "food",
            minRating = 4.0,
            page = 1,
            size = 10,
            sort = "rating",
        )

        assertEquals(expected, result.items)
        assertEquals("pho", capturedQuery)
        assertEquals("TP. Ho Chi Minh", capturedProvince)
        assertEquals("Q. 1", capturedDistrict)
        assertEquals("food", capturedType)
        assertEquals(4.0, capturedMinRating)
        assertEquals(1, capturedPage)
        assertEquals(10, capturedSize)
        assertEquals("rating", capturedSort)
    }
}