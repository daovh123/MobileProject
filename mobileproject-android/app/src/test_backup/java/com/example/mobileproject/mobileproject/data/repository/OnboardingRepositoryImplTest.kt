package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.ProductDto
import com.example.mobileproject.data.model.analytics.CategoryBreakdownDto
import com.example.mobileproject.data.model.analytics.SpendingTrendDto
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.LogoutResponseDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.data.model.favorite.FavoriteListResponseDto
import com.example.mobileproject.data.model.favorite.FavoriteToggleResponseDto
import com.example.mobileproject.data.model.favorite.HistoryListResponseDto
import com.example.mobileproject.data.model.goal.ContributeRequestDto
import com.example.mobileproject.data.model.goal.ContributeResponseDto
import com.example.mobileproject.data.model.goal.CreateGoalRequestDto
import com.example.mobileproject.data.model.goal.GoalResponseDto
import com.example.mobileproject.data.model.goal.SavingGoalDto
import com.example.mobileproject.data.model.map.MapLastLocationsResponseDto
import com.example.mobileproject.data.model.notification.FcmTokenRequestDto
import com.example.mobileproject.data.model.onboarding.AvatarFrameDto
import com.example.mobileproject.data.model.onboarding.AvatarFrameRequestDto
import com.example.mobileproject.data.model.onboarding.AvatarUploadResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestActionResponseDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestCreateRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleRequestDecisionRequestDto
import com.example.mobileproject.data.model.onboarding.CoupleStatusResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileResponseDto
import com.example.mobileproject.data.model.onboarding.ProfileUpsertRequestDto
import com.example.mobileproject.data.model.transaction.IncomeRequestDto
import com.example.mobileproject.data.model.transaction.TransactionDto
import com.example.mobileproject.data.model.transaction.TransactionRequestDto
import com.example.mobileproject.data.model.transaction.TransactionResponseDto
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class OnboardingRepositoryImplTest {

    @Test
    fun `saveProfile maps profile response including gender`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            profileResponse = Response.success(
                ProfileResponseDto(
                    success = true,
                    message = "Saved",
                    username = "alice",
                    fullName = "Alice",
                    nickName = "Ali",
                    birthDate = "2020-02-01",
                    gender = "FEMALE",
                    profileCompleted = true,
                    coupleConnected = false,
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())

        val result = repository.saveProfile(
            token = "token-x",
            fullName = "Alice",
            nickName = "Ali",
            birthDate = "2020-02-01",
            gender = "FEMALE",
        )

        assertEquals("alice", result.username)
        assertEquals("FEMALE", result.gender)
        assertTrue(result.profileCompleted)
    }

    @Test
    fun `getProfile maps profile response`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            profileResponse = Response.success(
                ProfileResponseDto(
                    success = true,
                    message = "Fetched",
                    username = "bob",
                    fullName = "Bob",
                    nickName = null,
                    birthDate = "2020-03-01",
                    gender = "MALE",
                    profileCompleted = true,
                    coupleConnected = true,
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())
        val profile = repository.getProfile("token-y")

        assertEquals("bob", profile.username)
        assertEquals("Bob", profile.fullName)
        assertEquals("2020-03-01", profile.birthDate)
        assertTrue(profile.profileCompleted)
        assertTrue(profile.coupleConnected)
        assertNull(profile.nickName)
    }

    @Test
    fun `getProfile throws message when api returns business failure`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            profileResponse = Response.success(
                ProfileResponseDto(
                    success = false,
                    message = "Unauthorized",
                    username = null,
                    fullName = null,
                    nickName = null,
                    birthDate = null,
                    gender = null,
                    profileCompleted = false,
                    coupleConnected = false,
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())

        val exception = runCatching { repository.getProfile("token-z") }.exceptionOrNull()
        assertTrue(exception is IllegalStateException)
        assertEquals("Unauthorized", exception?.message)
    }

    @Test
    fun `getCoupleStatus maps incoming pending request`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            coupleStatusResponse = Response.success(
                CoupleStatusResponseDto(
                    success = true,
                    message = "OK",
                    profileCompleted = true,
                    paired = false,
                    partnerUsername = null,
                    myCoupleCode = "123-456",
                    myCoupleCodeExpiresAt = "2030-01-01T00:00:00Z",
                    incomingRequestId = "req-1",
                    incomingRequesterUsername = "bob",
                    incomingRequesterDisplayName = "Bobby",
                    incomingCreatedAt = "2026-04-02T01:00:00Z",
                    outgoingRequestId = null,
                    outgoingRecipientUsername = null,
                    outgoingStatus = null,
                    outgoingUpdatedAt = null,
                    coupleId = null,
                    startAt = null,
                    daysTogether = null,
                    anniversaryTomorrow = null,
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())
        val status = repository.getCoupleStatus("token-y")

        assertEquals("123-456", status.myCoupleCode)
        assertEquals("2030-01-01T00:00:00Z", status.myCoupleCodeExpiresAt)
        assertEquals("req-1", status.incomingRequestId)
        assertEquals("bob", status.incomingRequesterUsername)
    }

    @Test
    fun `sendCoupleRequest throws message when api returns business failure`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            sendRequestResponse = Response.success(
                CoupleRequestActionResponseDto(
                    success = false,
                    message = "Partner code does not exist",
                    requestId = null,
                    status = null,
                    requesterUsername = null,
                    recipientUsername = null,
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())

        val exception = runCatching {
            repository.sendCoupleRequest("token-z", "111222")
        }.exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Partner code does not exist", exception?.message)
    }

    @Test
    fun `uploadAvatar returns avatar data url from response`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            avatarUploadResponse = Response.success(
                AvatarUploadResponseDto(
                    success = true,
                    message = "Uploaded",
                    avatarUrl = "data:image/png;base64,Zm9v",
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())
        val avatarUrl = repository.uploadAvatar("token-avatar", byteArrayOf(1, 2, 3), "image/png")

        assertEquals("data:image/png;base64,Zm9v", avatarUrl)
    }

    @Test
    fun `getAvatarFrames maps frames list`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            avatarFramesResponse = Response.success(
                listOf(
                    AvatarFrameDto(
                        id = "frame_rose",
                        name = "Rose",
                        resourceKey = "rose",
                        color = "#E91E63",
                    )
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())
        val frames = repository.getAvatarFrames("token-frames")

        assertEquals(1, frames.size)
        assertEquals("frame_rose", frames.first().id)
        assertEquals("#E91E63", frames.first().color)
    }

    @Test
    fun `setAvatarFrame maps profile response including avatarFrameId`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            setAvatarFrameResponse = Response.success(
                ProfileResponseDto(
                    success = true,
                    message = "Frame set",
                    username = "alice",
                    fullName = "Alice",
                    nickName = "Ali",
                    birthDate = "2020-02-01",
                    gender = "FEMALE",
                    profileCompleted = true,
                    coupleConnected = false,
                    email = "alice@example.com",
                    avatarUrl = "data:image/png;base64,Zm9v",
                    avatarFrameId = "frame_rose",
                )
            )
        }

        val repository = OnboardingRepositoryImpl(fakeApi, Gson())
        val profile = repository.setAvatarFrame("token-frame", "frame_rose")

        assertEquals("frame_rose", profile.avatarFrameId)
        assertEquals("data:image/png;base64,Zm9v", profile.avatarUrl)
    }

    private class FakeApiService : ApiService {

        var profileResponse: Response<ProfileResponseDto> = Response.success(
            ProfileResponseDto(false, "unused", null, null, null, null, null, false, false)
        )
        var coupleStatusResponse: Response<CoupleStatusResponseDto> = Response.success(
            CoupleStatusResponseDto(
                success = false,
                message = "unused",
                profileCompleted = false,
                paired = false,
                partnerUsername = null,
                myCoupleCode = null,
                myCoupleCodeExpiresAt = null,
                incomingRequestId = null,
                incomingRequesterUsername = null,
                incomingRequesterDisplayName = null,
                incomingCreatedAt = null,
                outgoingRequestId = null,
                outgoingRecipientUsername = null,
                outgoingStatus = null,
                outgoingUpdatedAt = null,
                coupleId = null,
                startAt = null,
                daysTogether = null,
                anniversaryTomorrow = null,
            )
        )
        var sendRequestResponse: Response<CoupleRequestActionResponseDto> = Response.success(
            CoupleRequestActionResponseDto(false, "unused", null, null, null, null)
        )
        var decideRequestResponse: Response<CoupleRequestActionResponseDto> = Response.success(
            CoupleRequestActionResponseDto(false, "unused", null, null, null, null)
        )
        var avatarUploadResponse: Response<AvatarUploadResponseDto> = Response.success(
            AvatarUploadResponseDto(false, "unused", null)
        )
        var avatarFramesResponse: Response<List<AvatarFrameDto>> = Response.success(emptyList())
        var setAvatarFrameResponse: Response<ProfileResponseDto> = profileResponse

        override suspend fun getProducts(): List<ProductDto> = emptyList()

        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
            return Response.success(AuthResponseDto(false, "unused", null, null, null))
        }

        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
            return Response.success(AuthResponseDto(false, "unused", null, null, null))
        }

        override suspend fun logout(authorization: String): Response<LogoutResponseDto> {
            return Response.success(LogoutResponseDto(false, "unused"))
        }

        override suspend fun upsertProfile(
            authorization: String,
            request: ProfileUpsertRequestDto,
        ): Response<ProfileResponseDto> {
            return profileResponse
        }

        override suspend fun getProfile(authorization: String): Response<ProfileResponseDto> {
            return profileResponse
        }

        override suspend fun getCoupleStatus(authorization: String): Response<CoupleStatusResponseDto> {
            return coupleStatusResponse
        }

        override suspend fun sendCoupleRequest(
            authorization: String,
            request: CoupleRequestCreateRequestDto,
        ): Response<CoupleRequestActionResponseDto> {
            return sendRequestResponse
        }

        override suspend fun decideCoupleRequest(
            authorization: String,
            requestId: String,
            request: CoupleRequestDecisionRequestDto,
        ): Response<CoupleRequestActionResponseDto> {
            return decideRequestResponse
        }

        override suspend fun toggleFavorite(
            authorization: String,
            placeId: String,
        ): Response<FavoriteToggleResponseDto> {
            return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
        }

        override suspend fun getFavorites(authorization: String): Response<FavoriteListResponseDto> {
            return Response.success(FavoriteListResponseDto(false, "unused", emptyList()))
        }

        override suspend fun checkFavorite(
            authorization: String,
            placeId: String,
        ): Response<FavoriteToggleResponseDto> {
            return Response.success(FavoriteToggleResponseDto(false, "unused", null, null, null))
        }

        override suspend fun recordHistory(
            authorization: String,
            placeId: String,
        ): Response<Unit> {
            return Response.success(Unit)
        }

        override suspend fun getHistory(authorization: String): Response<HistoryListResponseDto> {
            return Response.success(HistoryListResponseDto(false, "unused", emptyList()))
        }

        override suspend fun getMapLastLocations(authorization: String): Response<MapLastLocationsResponseDto> {
            return Response.success(MapLastLocationsResponseDto(false, "unused", null, null, null))
        }

        override suspend fun clearHistory(authorization: String): Response<Unit> {
            return Response.success(Unit)
        }

        override suspend fun registerFcmToken(
            authorization: String,
            request: FcmTokenRequestDto,
        ): Response<Unit> {
            return Response.success(Unit)
        }

        override suspend fun createTransaction(
            authorization: String,
            request: TransactionRequestDto,
        ): Response<TransactionResponseDto> {
            return Response.success(TransactionResponseDto(false, "unused", null, null, null, null, null, null, null))
        }

        override suspend fun processIncome(
            authorization: String,
            request: IncomeRequestDto,
        ): Response<TransactionResponseDto> {
            return Response.success(TransactionResponseDto(false, "unused", null, null, null, null, null, null, null))
        }

        override suspend fun getTransactions(
            authorization: String,
            coupleId: String,
        ): Response<List<TransactionDto>> {
            return Response.success(emptyList())
        }

        override suspend fun createGoal(
            authorization: String,
            request: CreateGoalRequestDto,
        ): Response<GoalResponseDto> {
            return Response.success(GoalResponseDto(false, "unused", null, null, null, null, null, null, null))
        }

        override suspend fun getGoalsByCouple(
            authorization: String,
            coupleId: String,
        ): Response<List<SavingGoalDto>> {
            return Response.success(emptyList())
        }

        override suspend fun contributeFromWallet(
            authorization: String,
            goalId: String,
            request: ContributeRequestDto,
        ): Response<ContributeResponseDto> {
            return Response.success(ContributeResponseDto(false, "unused", null, null, null, null, null, null))
        }

        override suspend fun contributeToGoal(
            authorization: String,
            goalId: String,
            request: ContributeRequestDto,
        ): Response<ContributeResponseDto> {
            return Response.success(ContributeResponseDto(false, "unused", null, null, null, null, null, null))
        }

        override suspend fun uploadAvatar(
            authorization: String,
            file: MultipartBody.Part,
        ): Response<AvatarUploadResponseDto> {
            return avatarUploadResponse
        }

        override suspend fun getAvatarFrames(authorization: String): Response<List<AvatarFrameDto>> {
            return avatarFramesResponse
        }

        override suspend fun setAvatarFrame(
            authorization: String,
            request: AvatarFrameRequestDto,
        ): Response<ProfileResponseDto> {
            return setAvatarFrameResponse
        }

        override suspend fun getCategoryBreakdown(
            authorization: String,
            coupleId: String,
            startDate: String,
            endDate: String,
        ): Response<List<CategoryBreakdownDto>> {
            return Response.success(emptyList())
        }

        override suspend fun getSpendingTrend(
            authorization: String,
            coupleId: String,
            year: Int,
            month: Int,
        ): Response<List<SpendingTrendDto>> {
            return Response.success(emptyList())
        }
    }
}
