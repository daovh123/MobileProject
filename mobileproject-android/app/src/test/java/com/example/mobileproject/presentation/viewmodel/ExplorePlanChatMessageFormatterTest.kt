package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.ExplorePlanItem
import com.example.mobileproject.domain.entity.Place
import org.junit.Assert.assertEquals
import org.junit.Test

class ExplorePlanChatMessageFormatterTest {

    @Test
    fun `build item includes stop metadata and readable map action`() {
        val message = ExplorePlanChatMessageFormatter.build(
            ExplorePlanItem(
                stopOrder = 2,
                experienceType = "drink",
                estimatedCost = 120_000L,
                reason = "Hop budget va dang duoc khen nhieu",
                place = Place(
                    id = "place-2",
                    name = "Tiem Nuong Hen Ho",
                    address = "12 Nguyen Hue",
                    district = "Quan 1",
                    province = "TP HCM",
                    effectiveTag = null,
                    category = null,
                    mealType = null,
                    rating = null,
                    reviewCount = null,
                    openHours = null,
                    priceRange = null,
                    imageUrl = null,
                    googleMapsUrl = "https://maps.google.com/?q=tiem+nuong",
                    lat = null,
                    lng = null,
                ),
            ),
        )

        assertEquals(
            "Goi y hen ho\nDiem dung: 2\nQuan: Tiem Nuong Hen Ho\nLoai trai nghiem: Cafe / do uong\nChi phi du kien: 120,000d\nLy do: Hop budget va dang duoc khen nhieu\nDia chi: 12 Nguyen Hue, Quan 1, TP HCM\nGoogle Maps: https://maps.google.com/?q=tiem+nuong",
            message,
        )
    }

    @Test
    fun `build uses direct google maps url when available`() {
        val message = ExplorePlanChatMessageFormatter.build(
            name = "Cafe Hen Ho",
            address = null,
            district = null,
            province = null,
            googleMapsUrl = "https://maps.google.com/?q=cafe",
            lat = 10.0,
            lng = 106.0,
        )

        assertEquals(
            "Goi y hen ho\nQuan: Cafe Hen Ho\nGoogle Maps: https://maps.google.com/?q=cafe",
            message,
        )
    }

    @Test
    fun `build falls back to coordinates link when lat lng available`() {
        val message = ExplorePlanChatMessageFormatter.build(
            name = "Bun Bo Date",
            address = null,
            district = null,
            province = null,
            googleMapsUrl = null,
            lat = 10.123456,
            lng = 106.654321,
        )

        assertEquals(
            "Goi y hen ho\nQuan: Bun Bo Date\nGoogle Maps: https://www.google.com/maps/search/?api=1&query=10.123456,106.654321",
            message,
        )
    }

    @Test
    fun `build falls back to address when no map link available`() {
        val message = ExplorePlanChatMessageFormatter.build(
            name = "Pho Toi",
            address = "12 Nguyen Hue",
            district = "Quan 1",
            province = "TP HCM",
            googleMapsUrl = null,
            lat = null,
            lng = null,
        )

        assertEquals(
            "Goi y hen ho\nQuan: Pho Toi\nDia chi: 12 Nguyen Hue, Quan 1, TP HCM",
            message,
        )
    }
}
