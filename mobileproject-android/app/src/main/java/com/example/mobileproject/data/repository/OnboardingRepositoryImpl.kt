package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.onboarding.*
import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
    private val gson: Gson,
) : OnboardingRepository {

    private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"

    override suspend fun saveProfile(
        token: String,
        fullName: String,
        nickName: String?,
        birthDate: String,
        gender: String,
        email: String?,
    ): ProfileResult {
        val response = apiService.upsertProfile(
            authorizationHeader(token),
            ProfileUpsertRequestDto(fullName, nickName, birthDate, gender, email)
        )
        val body = response.requireSuccessfulBody(gson, "Luu ho so that bai")
        authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected, null)
        return body.toProfileResult()
    }

    override suspend fun getProfile(token: String): ProfileResult {
        val response = apiService.getProfile(authorizationHeader(token))
        val body = response.requireSuccessfulBody(gson, "Khong the lay ho so")
        authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected, null)
        return body.toProfileResult()
    }

    override suspend fun getCoupleStatus(token: String): CoupleStatus {
        val response = apiService.getCoupleStatus(authorizationHeader(token))
        val body = response.body()
        if (response.isSuccessful && body != null) {
            // Cập nhật Store quan trọng để HomeScreen có dữ liệu coupleId
            authSessionStore.updateProfileState(
                profileCompleted = body.profileCompleted,
                coupleConnected = body.paired,
                coupleId = body.coupleId
            )
            return CoupleStatus(
                profileCompleted = body.profileCompleted,
                paired = body.paired,
                partnerUsername = body.partnerUsername,
                myCoupleCode = body.myCoupleCode,
                myCoupleCodeExpiresAt = body.myCoupleCodeExpiresAt,
                incomingRequestId = body.incomingRequestId,
                incomingRequesterUsername = body.incomingRequesterUsername,
                incomingRequesterDisplayName = body.incomingRequesterDisplayName,
                incomingCreatedAt = body.incomingCreatedAt,
                outgoingRequestId = body.outgoingRequestId,
                outgoingRecipientUsername = body.outgoingRecipientUsername,
                outgoingStatus = body.outgoingStatus,
                outgoingUpdatedAt = body.outgoingUpdatedAt,
                coupleId = body.coupleId,
                startAt = body.startAt,
                daysTogether = body.daysTogether ?: 0,
                anniversaryTomorrow = body.anniversaryTomorrow ?: false
            )
        } else {
            val errorMsg = response.parseErrorMessage(gson) ?: "Khong the tai trang thai ghep doi"
            throw Exception(errorMsg)
        }
    }

    override suspend fun getPartnerProfileSummary(token: String): PartnerProfileSummary {
        val response = apiService.getPartnerProfile(authorizationHeader(token))
        val body = response.body()
        if (response.isSuccessful && body != null) {
            return PartnerProfileSummary(
                paired = body.paired,
                message = body.message,
                username = body.username,
                fullName = body.fullName,
                nickName = body.nickName,
                avatarUrl = body.avatarUrl,
                startAt = body.startAt,
                daysTogether = body.daysTogether,
            )
        }
        val errorMsg = response.parseErrorMessage(gson) ?: "Khong the tai thong tin doi phuong"
        throw Exception(errorMsg)
    }

    override suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction {
        val response = apiService.sendCoupleRequest(authorizationHeader(token), CoupleRequestCreateRequestDto(partnerCode))
        val body = response.body()
        if (response.isSuccessful && body != null) {
            return CoupleRequestAction(
                requestId = body.requestId,
                status = body.status,
                message = body.message,
                requesterUsername = body.requesterUsername,
                recipientUsername = body.recipientUsername
            )
        } else {
            throw Exception(response.parseErrorMessage(gson) ?: "Gui yeu cau that bai")
        }
    }

    override suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction {
        val response = apiService.decideCoupleRequest(
            authorizationHeader(token),
            requestId,
            CoupleRequestDecisionRequestDto(accept = accept),
        )
        val body = response.requireSuccessfulBody(gson, "Xu ly yeu cau that bai")
        return CoupleRequestAction(
            requestId = body.requestId,
            status = body.status,
            message = body.message,
            requesterUsername = body.requesterUsername,
            recipientUsername = body.recipientUsername,
        )
    }

    override suspend fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String): String {
        val requestBody = imageBytes.toRequestBody(contentType.toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", "avatar", requestBody)
        val response = apiService.uploadAvatar(authorizationHeader(token), filePart)
        val body = response.requireAvatarUploadBody(gson, "Tai anh that bai")
        return body.avatarUrl ?: ""
    }

    override suspend fun getAvatarFrames(token: String): List<AvatarFrame> {
        val response = apiService.getAvatarFrames(authorizationHeader(token))
        return response.body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult {
        val response = apiService.setAvatarFrame(authorizationHeader(token), AvatarFrameRequestDto(frameId))
        val body = response.requireSuccessfulBody(gson, "Dat khung anh that bai")
        return body.toProfileResult()
    }
}

private fun ProfileResponseDto.toProfileResult() = ProfileResult(
    username = username, fullName = fullName, nickName = nickName, birthDate = birthDate,
    gender = gender, profileCompleted = profileCompleted, coupleConnected = coupleConnected,
    email = email, avatarUrl = avatarUrl, avatarFrameId = avatarFrameId,
)

private fun AvatarFrameDto.toDomain() = AvatarFrame(id, name, resourceKey, color)

private fun Response<ProfileResponseDto>.requireSuccessfulBody(gson: Gson, defaultMsg: String): ProfileResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<CoupleStatusResponseDto>.requireSuccessfulBody(gson: Gson, defaultMsg: String): CoupleStatusResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<CoupleRequestActionResponseDto>.requireSuccessfulBody(gson: Gson, defaultMsg: String): CoupleRequestActionResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<AvatarUploadResponseDto>.requireAvatarUploadBody(gson: Gson, defaultMsg: String): AvatarUploadResponseDto {
    val b = body()
    if (!isSuccessful || b == null || !b.success) throw IllegalStateException(b?.message ?: parseErrorMessage(gson) ?: defaultMsg)
    return b
}

private fun Response<*>.parseErrorMessage(gson: Gson): String? {
    return runCatching { errorBody()?.charStream()?.use { gson.fromJson(it, ErrorMessageDto::class.java).message } }.getOrNull()
}

class ErrorMessageDto(val message: String)
