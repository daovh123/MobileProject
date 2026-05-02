package com.example.mobileproject.data.model.moment

data class MomentDto(
    val id: String?,
    val coupleId: String?,
    val title: String,
    val imageUrl: String,
    val createdAt: String?,
)

data class MomentRequestDto(
    val coupleId: String,
    val title: String,
    val base64Image: String,
)

data class MomentResponseDto(
    val success: Boolean,
    val message: String,
    val moment: MomentDto?,
)
