package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestActionResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleStatusResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
) : OnboardingRepository {

    override suspend fun saveProfile(
        token: String,
        fullName: String,
        nickName: String?,
        birthDate: String,
        gender: String,
    ): ProfileResult {
        val response = apiService.upsertProfile(
            authorizationHeader(token),
            ProfileUpsertRequestDto(
                fullName = fullName,
                nickName = nickName,
                birthDate = birthDate,
                gender = gender,
            )
        )
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Luu ho so that bai")

        return ProfileResult(
            username = body.username,
            fullName = body.fullName,
            nickName = body.nickName,
            birthDate = body.birthDate,
            gender = body.gender,
            profileCompleted = body.profileCompleted,
            coupleConnected = body.coupleConnected,
        )
    }

    override suspend fun getProfile(token: String): ProfileResult {
        val response = apiService.getProfile(authorizationHeader(token))
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Khong the lay ho so")

        return ProfileResult(
            username = body.username,
            fullName = body.fullName,
            nickName = body.nickName,
            birthDate = body.birthDate,
            gender = body.gender,
            profileCompleted = body.profileCompleted,
            coupleConnected = body.coupleConnected,
        )
    }

    override suspend fun getCoupleStatus(token: String): CoupleStatus {
        val response = apiService.getCoupleStatus(authorizationHeader(token))
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Khong the lay trang thai ghep doi")

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
            daysTogether = body.daysTogether,
            anniversaryTomorrow = body.anniversaryTomorrow,
        )
    }

    override suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction {
        val response = apiService.sendCoupleRequest(
            authorizationHeader(token),
            CoupleRequestCreateRequestDto(partnerCode = partnerCode),
        )
        val body = response.requireSuccessfulBody(gson, defaultFailureMessage = "Gui loi moi ghep doi that bai")

        return CoupleRequestAction(
            requestId = body.requestId,
            status = body.status,
            message = body.message,
            requesterUsername = body.requesterUsername,
            recipientUsername = body.recipientUsername,
        )
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

    private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"
}

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

private fun Response<*>.parseErrorMessage(gson: Gson): String? {
    return runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, ErrorMessageDto::class.java).message
        }
    }.getOrNull()?.takeIf { !it.isNullOrBlank() }
}

private data class ErrorMessageDto(
    val message: String? = null,
)
