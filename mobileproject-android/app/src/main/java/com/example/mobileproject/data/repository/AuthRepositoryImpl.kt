package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.LogoutResponseDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject

/**
 * Implementation của [AuthRepository], xử lý xác thực người dùng.
 *
 * ## Caching strategy
 * - Lưu session vào [AuthSessionStore] (SharedPreferences) sau khi login thành công
 * - Xóa session local khi logout (dù API call có thành công hay không)
 *
 * ## Error handling
 * - Parse error body từ server (JSON) để lấy message lỗi chi tiết
 * - Throw [IllegalStateException] với message từ server hoặc message mặc định
 *
 * ## Threading
 * Các hàm là `suspend`, chạy trên dispatcher của Retrofit (IO dispatcher theo mặc định)
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val authSessionStore: AuthSessionStore? = null,
) : AuthRepository {

    /**
     * Đăng nhập bằng username hoặc email.
     *
     * Quy trình: gọi API login -> parse response -> lưu session vào local store.
     *
     * @param usernameOrEmail tên đăng nhập hoặc email
     * @param password mật khẩu
     * @return [AuthSession] chứa token và thông tin người dùng
     * @throws IllegalStateException nếu đăng nhập thất bại (sai thông tin, server error)
     */
    override suspend fun login(usernameOrEmail: String, password: String): AuthSession {
        val response = apiService.login(
            LoginRequestDto(
                usernameOrEmail = usernameOrEmail,
                password = password,
            )
        )
        val session = response.toAuthSessionOrThrow(gson, defaultFailureMessage = "Dang nhap that bai")
        authSessionStore?.save(session)
        return session
    }

    /**
     * Đăng ký tài khoản mới.
     *
     * Lưu ý: Không tự động lưu session (người dùng cần login sau khi đăng ký).
     *
     * @param username tên đăng nhập
     * @param email địa chỉ email
     * @param password mật khẩu
     * @return [AuthSession] chứa token và thông tin tài khoản
     * @throws IllegalStateException nếu đăng ký thất bại (trùng username/email, validation)
     */
    override suspend fun register(username: String, email: String, password: String): AuthSession {
        val response = apiService.register(
            RegisterRequestDto(
                username = username,
                email = email,
                password = password,
            )
        )
        return response.toAuthSessionOrThrow(gson, defaultFailureMessage = "Dang ky that bai")
    }

    /**
     * Đăng xuất khỏi hệ thống.
     *
     * Luôn xóa local session (dù API call thất bại) để đảm bảo người dùng
     * không bị "kẹt" trong trạng thái đăng nhập trên client.
     *
     * @param token JWT token hiện tại
     * @throws IllegalStateException nếu API call thất bại (sau khi đã xóa local)
     */
    override suspend fun logout(token: String) {
        val logoutResult = runCatching {
            val response = apiService.logout(authorizationHeader(token))
            response.toLogoutSuccessOrThrow(gson, defaultFailureMessage = "Dang xuat that bai")
        }

        authSessionStore?.clear()
        logoutResult.getOrThrow()
    }
}

private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"

private fun Response<AuthResponseDto>.toAuthSessionOrThrow(
    gson: Gson,
    defaultFailureMessage: String,
): AuthSession {
    val bodyOrError = body() ?: parseErrorBody(gson)
    val message = bodyOrError?.message?.ifBlank { defaultFailureMessage } ?: defaultFailureMessage

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }

    val token = bodyOrError.token ?: throw IllegalStateException("$message: missing token")
    val username = bodyOrError.username ?: throw IllegalStateException("$message: missing username")
    val email = bodyOrError.email ?: throw IllegalStateException("$message: missing email")

    return AuthSession(
        token = token,
        username = username,
        email = email,
        profileCompleted = bodyOrError.profileCompleted ?: false,
        coupleConnected = bodyOrError.coupleConnected ?: false,
    )
}

private fun Response<AuthResponseDto>.parseErrorBody(gson: Gson): AuthResponseDto? {
    return runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, AuthResponseDto::class.java)
        }
    }.getOrNull()
}

private fun Response<LogoutResponseDto>.toLogoutSuccessOrThrow(
    gson: Gson,
    defaultFailureMessage: String,
) {
    val bodyOrError = body()
    val message = when {
        bodyOrError != null -> bodyOrError.message
        else -> parseErrorMessage(gson) ?: defaultFailureMessage
    }.ifBlank { defaultFailureMessage }

    if (!isSuccessful || bodyOrError == null || !bodyOrError.success) {
        throw IllegalStateException(message)
    }
}

private fun Response<*>.parseErrorMessage(gson: Gson): String? {
    return runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, MessageErrorDto::class.java).message
        }
    }.getOrNull()?.takeIf { !it.isNullOrBlank() }
}

private data class MessageErrorDto(
    val message: String? = null,
)
