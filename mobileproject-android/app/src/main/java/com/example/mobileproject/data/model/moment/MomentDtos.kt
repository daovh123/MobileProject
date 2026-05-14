package com.example.mobileproject.data.model.moment

data class MomentDto(
    val id: String?,
    val coupleId: String?,
    val title: String,
    val imageUrl: String,
    val createdAt: String?,
    val reactionsCount: Int = 0,
    val commentsCount: Int = 0,
    val viewerReaction: String? = null,
    val reactions: List<MomentReactionSummaryDto> = emptyList(),
)

data class MomentReactionSummaryDto(
    val reaction: String,
    val count: Int,
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

data class MomentReactionRequestDto(
    val reaction: String,
)

data class MomentReactionResponseDto(
    val momentId: String,
    val viewerReaction: String?,
    val reactionsCount: Int,
    val reactions: List<MomentReactionSummaryDto> = emptyList(),
)

data class MomentCommentRequestDto(
    val content: String,
)

data class MomentCommentDto(
    val id: String?,
    val momentId: String,
    val authorUsername: String,
    val content: String,
    val createdAt: String?,
)
