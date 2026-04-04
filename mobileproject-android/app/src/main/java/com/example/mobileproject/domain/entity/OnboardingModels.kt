package com.example.mobileproject.domain.entity

data class ProfileResult(
    val username: String?,
    val fullName: String?,
    val nickName: String?,
    val birthDate: String?,
    val gender: String?,
    val profileCompleted: Boolean,
    val coupleConnected: Boolean,
)

data class CoupleRequestAction(
    val requestId: String?,
    val status: String?,
    val message: String,
    val requesterUsername: String?,
    val recipientUsername: String?,
)

data class CoupleStatus(
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
    val coupleId: String?,
    val startAt: String?,
    val daysTogether: Long?,
    val anniversaryTomorrow: Boolean?,
)
