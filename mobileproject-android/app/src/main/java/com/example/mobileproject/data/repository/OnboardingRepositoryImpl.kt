package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
) : OnboardingRepository {

    private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"

    override suspend fun saveProfile(
        token: String,
        fullName: String,
        nickName: String?,
        birthDate: String,
        gender: String
    ): ProfileResult {
        val response = apiService.upsertProfile(
            authorizationHeader(token),
            ProfileUpsertRequestDto(fullName, nickName, birthDate, gender)
        )
        val body = response.body()
        if (response.isSuccessful && body != null) {
            authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected)
            return ProfileResult(
                username = body.username,
                fullName = body.fullName,
                nickName = body.nickName,
                birthDate = body.birthDate,
                gender = body.gender,
                profileCompleted = body.profileCompleted,
                coupleConnected = body.coupleConnected
            )
        } else {
            throw Exception(response.message())
        }
    }

    override suspend fun getProfile(token: String): ProfileResult {
        val response = apiService.getProfile(authorizationHeader(token))
        val body = response.body()
        if (response.isSuccessful && body != null) {
            authSessionStore.updateProfileState(body.profileCompleted, body.coupleConnected)
            return ProfileResult(
                username = body.username,
                fullName = body.fullName,
                nickName = body.nickName,
                birthDate = body.birthDate,
                gender = body.gender,
                profileCompleted = body.profileCompleted,
                coupleConnected = body.coupleConnected
            )
        } else {
            throw Exception(response.message())
        }
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
        val response = apiService.decideCoupleRequest(authorizationHeader(token), requestId, CoupleRequestDecisionRequestDto(accept))
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
}