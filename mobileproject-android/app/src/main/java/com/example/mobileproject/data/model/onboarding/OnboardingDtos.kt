package com.example.mobileproject.data.model.onboarding

import com.google.gson.annotations.SerializedName

data class ProfileUpsertRequestDto(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("nickName") val nickName: String?,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("email") val email: String? = null,
)

data class ProfileResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("username") val username: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("nickName") val nickName: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("profileCompleted") val profileCompleted: Boolean,
    @SerializedName(value = "coupleConnected", alternate = ["paired"]) val coupleConnected: Boolean,
    @SerializedName("email") val email: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("avatarFrameId") val avatarFrameId: String? = null,
)

data class CoupleStatusResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("profileCompleted") val profileCompleted: Boolean,
    @SerializedName(value = "paired", alternate = ["coupleConnected"]) val paired: Boolean,
    @SerializedName("partnerUsername") val partnerUsername: String?,
    @SerializedName("myCoupleCode") val myCoupleCode: String?,
    @SerializedName("myCoupleCodeExpiresAt") val myCoupleCodeExpiresAt: String? = null,
    @SerializedName("coupleId") val coupleId: String? = null,
    @SerializedName("startAt") val startAt: String? = null,
    @SerializedName("daysTogether") val daysTogether: Long? = null,
    @SerializedName("anniversaryTomorrow") val anniversaryTomorrow: Boolean? = null,
    @SerializedName("incomingRequestId") val incomingRequestId: String? = null,
    @SerializedName("incomingRequesterUsername") val incomingRequesterUsername: String? = null,
    @SerializedName("incomingRequesterDisplayName") val incomingRequesterDisplayName: String? = null,
    @SerializedName("incomingCreatedAt") val incomingCreatedAt: String? = null,
    @SerializedName("outgoingRequestId") val outgoingRequestId: String? = null,
    @SerializedName("outgoingRecipientUsername") val outgoingRecipientUsername: String? = null,
    @SerializedName("outgoingStatus") val outgoingStatus: String? = null,
    @SerializedName("outgoingUpdatedAt") val outgoingUpdatedAt: String? = null,
)

data class CouplePartnerProfileResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("paired") val paired: Boolean,
    @SerializedName("username") val username: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("nickName") val nickName: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("startAt") val startAt: String?,
    @SerializedName("daysTogether") val daysTogether: Long?,
)

data class CoupleRequestCreateRequestDto(
    @SerializedName("partnerCode") val partnerCode: String,
)

data class CoupleRequestDecisionRequestDto(
    @SerializedName("accept") val accept: Boolean,
)

data class CoupleRequestActionResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("requestId") val requestId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("requesterUsername") val requesterUsername: String?,
    @SerializedName("recipientUsername") val recipientUsername: String?,
)

data class AvatarUploadResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
)

data class AvatarFrameDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("resourceKey") val resourceKey: String,
    @SerializedName("color") val color: String,
)

data class AvatarFrameRequestDto(
    @SerializedName("frameId") val frameId: String?,
)
