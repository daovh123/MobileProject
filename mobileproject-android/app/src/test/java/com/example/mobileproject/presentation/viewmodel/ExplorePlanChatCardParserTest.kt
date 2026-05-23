package com.example.mobileproject.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExplorePlanChatCardParserTest {

    @Test
    fun `parse returns structured card for explore plan message`() {
        val message = """
            Goi y hen ho
            Diem dung: 2
            Quan: Tiem Nuong Hen Ho
            Loai trai nghiem: An chinh
            Chi phi du kien: 120,000d
            Ly do: Hop budget • Danh gia cao
            Dia chi: 12 Nguyen Hue, Quan 1, TP HCM
            Google Maps: https://www.google.com/maps/search/?api=1&query=10.0,106.0
        """.trimIndent()

        val parsed = ExplorePlanChatCardParser.parse(message)

        requireNotNull(parsed)
        assertEquals(2, parsed.stopOrder)
        assertEquals("Tiem Nuong Hen Ho", parsed.title)
        assertEquals("An chinh", parsed.experienceType)
        assertEquals("120,000d", parsed.estimatedCostLabel)
        assertEquals("Hop budget • Danh gia cao", parsed.reason)
        assertEquals("12 Nguyen Hue, Quan 1, TP HCM", parsed.address)
        assertEquals("https://www.google.com/maps/search/?api=1&query=10.0,106.0", parsed.googleMapsUrl)
    }

    @Test
    fun `parse returns null for normal chat message`() {
        assertNull(ExplorePlanChatCardParser.parse("Toi dang tren duong ve"))
    }
}
