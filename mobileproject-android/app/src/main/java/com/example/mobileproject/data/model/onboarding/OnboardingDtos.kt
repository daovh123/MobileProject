package com.example.mobileproject.data.model.onboarding

data class ProfileUpsertRequestDto(
    val fullName: String,
    val nickName: String?,
    val birthDate: String,
    val gender: String,
)

data class ProfileResponseDto(
    val success: Boolean,
    val message: String,
    val username: String?,
    val fullName: String?,
    val nickName: String?,
    val birthDate: String?,
    val gender: String?,
    val profileCompleted: Boolean,
    val coupleConnected: Boolean,
)

data class CoupleCodeResponseDto(
    val success: Boolean,
    val message: String,
    val myCode: String?,
    val myCodeExpiresAt: String? = null,
)

data class CoupleRequestCreateRequestDto(
    val partnerCode: String,
)

data class CoupleRequestDecisionRequestDto(
    val accept: Boolean,
)

data class CoupleRequestActionResponseDto(
    val success: Boolean,
    val message: String,
    val requestId: String?,
    val status: String?,
    val requesterUsername: String?,
    val recipientUsername: String?,
)

data class CoupleStatusResponseDto(
    val success: Boolean,
    val message: String,
    val profileCompleted: Boolean,
    val paired: Boolean,
    val partnerUsername: String?,
    val myCoupleCode: String?,
    val myCoupleCodeExpiresAt: String? = null,
    val incomingRequestId: String?,
    val incomingRequesterUsername: String?,
    val incomingRequesterDisplayName: String?,
    val incomingCreatedAt: String?,
    val outgoingRequestId: String?,
    val outgoingRecipientUsername: String?,
    val outgoingStatus: String?,
    val outgoingUpdatedAt: String?,
)
