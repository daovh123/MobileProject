package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.onboarding.AvatarFrameDto
import com.example.mobileproject.data.model.onboarding.AvatarFrameRequestDto
import com.example.mobileproject.data.model.onboarding.AvatarUploadResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
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
        email: String? = null,
    ): ProfileResult {
        val response = apiService.upsertProfile(
            authorizationHeader(token),
            ProfileUpsertRequestDto(
                fullName = fullName,
                nickName = nickName,
                birthDate = birthDate,
                gender = gender,
                email = email,
            )
        )
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Luu ho so that bai")
        authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected)
        return body.toProfileResult()
    }

    override suspend fun getProfile(token: String): ProfileResult {
        val response = apiService.getProfile(authorizationHeader(token))
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Khong the lay ho so")
        authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected)
        return body.toProfileResult()
    }

    override suspend fun getCoupleStatus(token: String): CoupleStatus {
        val response = apiService.getCoupleStatus(authorizationHeader(token))
        val body = response.body()
        if (response.isSuccessful && body != null) {
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
            throw Exception(response.message())
        }
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
            throw Exception(response.message())
        }
    }

    override suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction {
        val response = apiService.decideCoupleRequest(
            authorizationHeader(token),
            requestId,
            CoupleRequestDecisionRequestDto(accept = accept),
        )
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Xu ly yeu cau ghep doi that bai")

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
        val body = response.requireAvatarUploadBody(gson, defaultFailureMessage = "Tai anh that bai")
        return body.avatarUrl ?: throw IllegalStateException("Khong nhan duoc URL anh dai dien")
    }

    override suspend fun getAvatarFrames(token: String): List<AvatarFrame> {
        val response = apiService.getAvatarFrames(authorizationHeader(token))
        val body = response.requireFramesBody(gson, defaultFailureMessage = "Khong the lay danh sach khung anh")
        return body.map { it.toDomain() }
    }

    override suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult {
        val response = apiService.setAvatarFrame(
            authorizationHeader(token),
            AvatarFrameRequestDto(frameId = frameId),
        )
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Dat khung anh that bai")
        return body.toProfileResult()
    }
}

private fun ProfileResponseDto.toProfileResult() = ProfileResult(
    username = username,
    fullName = fullName,
    nickName = nickName,
    birthDate = birthDate,
    gender = gender,
    profileCompleted = profileCompleted,
    coupleConnected = coupleConnected,
    email = email,
    avatarUrl = avatarUrl,
    avatarFrameId = avatarFrameId,
)

private fun AvatarFrameDto.toDomain() = AvatarFrame(
    id = id,
    name = name,
    resourceKey = resourceKey,
    color = color,
)

private fun Response<ProfileResponseDto>.requireSuccessfulBody(
    gson: Gson,
    defaultFailureMessage: String,
): ProfileResponseDto {
    val bodyOrError = body()
    val message = when {
        bodyOrError != null -> bodyOrError.message
        else -> parseErrorMessage(gson) ?: defaultFailureMessage
    }.ifBlank { defaultFailureMessage }

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }

    return bodyOrError
}

private fun Response<CoupleStatusResponseDto>.requireSuccessfulBody(
    gson: Gson,
    defaultFailureMessage: String,
): CoupleStatusResponseDto {
    val bodyOrError = body()
    val message = when {
        bodyOrError != null -> bodyOrError.message
        else -> parseErrorMessage(gson) ?: defaultFailureMessage
    }.ifBlank { defaultFailureMessage }

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }

    return bodyOrError
}

private fun Response<CoupleRequestActionResponseDto>.requireSuccessfulBody(
    gson: Gson,
    defaultFailureMessage: String,
): CoupleRequestActionResponseDto {
    val bodyOrError = body()
    val message = when {
        bodyOrError != null -> bodyOrError.message
        else -> parseErrorMessage(gson) ?: defaultFailureMessage
    }.ifBlank { defaultFailureMessage }

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }

    return bodyOrError
}

private fun Response<AvatarUploadResponseDto>.requireAvatarUploadBody(
    gson: Gson,
    defaultFailureMessage: String,
): AvatarUploadResponseDto {
    val bodyOrError = body()
    val message = when {
        bodyOrError != null -> bodyOrError.message
        else -> parseErrorMessage(gson) ?: defaultFailureMessage
    }.ifBlank { defaultFailureMessage }

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }

    return bodyOrError
}

private fun Response<List<AvatarFrameDto>>.requireFramesBody(
    gson: Gson,
    defaultFailureMessage: String,
): List<AvatarFrameDto> {
    if (!isSuccessful || body() == null) {
        val errorMsg = parseErrorMessage(gson) ?: defaultFailureMessage
        throw IllegalStateException(errorMsg)
    }
    return body()!!
}

class ErrorMessageDto(val message: String)

private fun Response<*>.parseErrorMessage(gson: Gson): String? {
    return runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, ErrorMessageDto::class.java).message
        }
    }.getOrNull()
}