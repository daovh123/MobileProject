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
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthRepositoryImplTest {

    @Test
    fun `login returns mapped auth session when api returns success`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            loginResponse = Response.success(
                AuthResponseDto(
                    success = true,
                    message = "Login successful",
                    token = "token-123",
                    username = "alice",
                    email = "alice@example.com",
                    profileCompleted = true,
                    coupleConnected = false,
                )
            )
        }

        val repository = AuthRepositoryImpl(fakeApi, Gson())
        val result = repository.login("alice@example.com", "secret")

        assertEquals("token-123", result.token)
        assertEquals("alice", result.username)
        assertEquals("alice@example.com", result.email)
        assertTrue(result.profileCompleted)
        assertTrue(!result.coupleConnected)
    }

    @Test
    fun `login throws message from api response when login fails`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            loginResponse = Response.success(
                AuthResponseDto(
                    success = false,
                    message = "Invalid credentials",
                    token = null,
                    username = null,
                    email = null,
                    profileCompleted = false,
                    coupleConnected = false,
                )
            )
        }

        val repository = AuthRepositoryImpl(fakeApi, Gson())

        val exception = runCatching { repository.login("alice@example.com", "wrong") }
            .exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Invalid credentials", exception?.message)
    }

    @Test
    fun `register throws when success response is missing token`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            registerResponse = Response.success(
                AuthResponseDto(
                    success = true,
                    message = "Registered",
                    token = null,
                    username = "bob",
                    email = "bob@example.com",
                    profileCompleted = false,
                    coupleConnected = false,
                )
            )
        }

        val repository = AuthRepositoryImpl(fakeApi, Gson())

        val exception = runCatching { repository.register("bob", "bob@example.com", "secret") }
            .exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Registered: missing token", exception?.message)
    }

    @Test
    fun `logout throws message when api returns failure`() = runBlocking {
        val fakeApi = FakeApiService().apply {
            logoutResponse = Response.success(
                LogoutResponseDto(success = false, message = "Invalid access token")
            )
        }

        val repository = AuthRepositoryImpl(fakeApi, Gson())

        val exception = runCatching { repository.logout("token-logout") }
            .exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertEquals("Invalid access token", exception?.message)
    }

    private class FakeApiService : ApiService {
        var loginResponse: Response<AuthResponseDto> =
            Response.success(AuthResponseDto(false, "unused", null, null, null))

        var registerResponse: Response<AuthResponseDto> =
            Response.success(AuthResponseDto(false, "unused", null, null, null))

        var logoutResponse: Response<LogoutResponseDto> =
            Response.success(LogoutResponseDto(false, "unused"))

        private val profileResponse: Response<ProfileResponseDto> =
            Response.success(ProfileResponseDto(false, "unused", null, null, null, null, null, false, false))

        private val coupleStatusResponse: Response<CoupleStatusResponseDto> =
            Response.success(
                CoupleStatusResponseDto(
                    success = false,
                    message = "unused",
                    profileCompleted = false,
                    paired = false,
                    partnerUsername = null,
                    myCoupleCode = null,
                    incomingRequestId = null,
                    incomingRequesterUsername = null,
                    incomingRequesterDisplayName = null,
                    incomingCreatedAt = null,
                    outgoingRequestId = null,
                    outgoingRecipientUsername = null,
                    outgoingStatus = null,
                    outgoingUpdatedAt = null,
                )
            )

        override suspend fun getProducts(): List<ProductDto> = emptyList()

        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> = loginResponse

        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> = registerResponse

        override suspend fun logout(authorization: String): Response<LogoutResponseDto> = logoutResponse

        override suspend fun upsertProfile(
            authorization: String,
            request: ProfileUpsertRequestDto,
        ): Response<ProfileResponseDto> = profileResponse

        override suspend fun getProfile(authorization: String): Response<ProfileResponseDto> = profileResponse

        override suspend fun getCoupleStatus(authorization: String): Response<CoupleStatusResponseDto> =
            coupleStatusResponse

        override suspend fun sendCoupleRequest(
            authorization: String,
            request: CoupleRequestCreateRequestDto,
        ): Response<CoupleRequestActionResponseDto> {
            return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
        }

        override suspend fun decideCoupleRequest(
            authorization: String,
            requestId: String,
            request: CoupleRequestDecisionRequestDto,
        ): Response<CoupleRequestActionResponseDto> {
            return Response.success(CoupleRequestActionResponseDto(false, "unused", null, null, null, null))
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
            return Response.success(AvatarUploadResponseDto(false, "unused", null))
        }

        override suspend fun getAvatarFrames(authorization: String): Response<List<AvatarFrameDto>> {
            return Response.success(emptyList())
        }

        override suspend fun setAvatarFrame(
            authorization: String,
            request: AvatarFrameRequestDto,
        ): Response<ProfileResponseDto> {
            return profileResponse
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
