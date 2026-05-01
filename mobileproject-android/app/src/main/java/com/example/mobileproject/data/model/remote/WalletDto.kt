package com.example.mobileproject.data.model.remote

import com.google.gson.annotations.SerializedName

// Backend trả về trực tiếp Object, không có bọc success/data
data class WalletResponse(
    @SerializedName("idCouple") val idCouple: String?,
    @SerializedName("walletName") val walletName: String?,
    @SerializedName("totalBalance") val totalBalance: Long?
)