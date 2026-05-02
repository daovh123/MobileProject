package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.ProfileResult

interface OnboardingRepository {

    suspend fun saveProfile(
        token: String,
        fullName: String,
        nickName: String?,
        birthDate: String,
        gender: String,
        email: String? = null,
    ): ProfileResult

    suspend fun getProfile(token: String): ProfileResult

    suspend fun getCoupleStatus(token: String): CoupleStatus

    suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction

    suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction

    suspend fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String): String

    suspend fun getAvatarFrames(token: String): List<AvatarFrame>

    suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult
}
