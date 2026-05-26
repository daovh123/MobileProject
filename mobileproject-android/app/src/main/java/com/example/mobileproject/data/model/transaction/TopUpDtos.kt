package com.example.mobileproject.data.model.transaction

import com.google.gson.annotations.SerializedName

data class TopUpCreateRequestDto(
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("bankId") val bankId: String,
    @SerializedName("bankName") val bankName: String,
    @SerializedName("note") val note: String?,
)

data class TopUpResponseDto(
    @SerializedName("id") val id: String?,
    @SerializedName("coupleId") val coupleId: String?,
    @SerializedName("amount") val amount: Long?,
    @SerializedName("status") val status: String?,
    @SerializedName("bankId") val bankId: String?,
    @SerializedName("bankName") val bankName: String?,
    @SerializedName("accountNumber") val accountNumber: String?,
    @SerializedName("accountName") val accountName: String?,
    @SerializedName("transferCode") val transferCode: String?,
    @SerializedName("transferContent") val transferContent: String?,
    @SerializedName("qrContent") val qrContent: String?,
    @SerializedName("qrImageUrl") val qrImageUrl: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("currentBalance") val currentBalance: Long?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("paidAt") val paidAt: String?,
)
