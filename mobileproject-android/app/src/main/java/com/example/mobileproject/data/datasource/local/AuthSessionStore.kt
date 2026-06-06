package com.example.mobileproject.data.datasource.local

import android.content.Context
import com.example.mobileproject.domain.entity.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local storage cho phiên đăng nhập người dùng, sử dụng [SharedPreferences].
 *
 * ## Thông tin được lưu trữ
 * - **Token**: JWT token xác thực API
 * - **Username**: tên đăng nhập
 * - **Email**: địa chỉ email
 * - **Profile completed**: trạng thái đã hoàn thành hồ sơ hay chưa
 * - **Couple connected**: trạng thái đã ghép đôi hay chưa
 * - **Couple ID**: ID của cặp đôi (nếu đã ghép)
 *
 * ## Thread safety
 * SharedPreferences hoạt động trên main thread, các thao tác ghi sử dụng `apply()` (bất đồng bộ).
 *
 * ## Lifecycle
 * Singleton, tồn tại suốt vòng đời ứng dụng. Dùng [clear] khi đăng xuất để xóa toàn bộ dữ liệu.
 */
@Singleton
class AuthSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Lưu toàn bộ thông tin phiên đăng nhập vào SharedPreferences.
     *
     * Lưu ý: sử dụng `apply()` nên thao tác ghi là bất đồng bộ, không block main thread.
     *
     * @param session phiên đăng nhập cần lưu
     */
    fun save(session: AuthSession) {
        preferences.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_USERNAME, session.username)
            .putString(KEY_EMAIL, session.email)
            .putBoolean(KEY_PROFILE_COMPLETED, session.profileCompleted)
            .putBoolean(KEY_COUPLE_CONNECTED, session.coupleConnected)
            .putString(KEY_COUPLE_ID, session.coupleId)
            .apply()
    }

    /**
     * Đọc phiên đăng nhập đã lưu từ SharedPreferences.
     *
     * Trả về `null` nếu chưa đăng nhập (token không tồn tại hoặc rỗng).
     * Các trường phụ (username, email...) có giá trị mặc định là chuỗi rỗng nếu chưa lưu.
     *
     * @return [AuthSession] nếu đã đăng nhập, `null` nếu chưa
     */
    fun load(): AuthSession? {
        val token = preferences.getString(KEY_TOKEN, null)?.trim() ?: return null
        if (token.isBlank()) return null

        return AuthSession(
            token = token,
            username = preferences.getString(KEY_USERNAME, "").orEmpty(),
            email = preferences.getString(KEY_EMAIL, "").orEmpty(),
            profileCompleted = preferences.getBoolean(KEY_PROFILE_COMPLETED, false),
            coupleConnected = preferences.getBoolean(KEY_COUPLE_CONNECTED, false),
            coupleId = preferences.getString(KEY_COUPLE_ID, null)
        )
    }

    /**
     * Cập nhật trạng thái hồ sơ và ghép đôi mà không thay đổi token/username/email.
     *
     * Dùng sau khi người dùng hoàn thành hồ sơ hoặc ghép đôi thành công.
     * Nếu chưa có session tồn tại, hàm sẽ bỏ qua (không làm gì).
     *
     * @param profileCompleted hồ sơ đã hoàn thành hay chưa
     * @param coupleConnected đã ghép đôi hay chưa
     * @param coupleId ID cặp đôi mới (giữ nguyên nếu null)
     */
    fun updateProfileState(profileCompleted: Boolean, coupleConnected: Boolean, coupleId: String? = null) {
        val existing = load() ?: return
        save(
            existing.copy(
                profileCompleted = profileCompleted,
                coupleConnected = coupleConnected,
                coupleId = coupleId ?: existing.coupleId
            )
        )
    }

    /**
     * Xóa toàn bộ dữ liệu phiên đăng nhập.
     * Gọi khi đăng xuất để đảm bảo không còn token cũ trên thiết bị.
     */
    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        private const val PREFS_NAME = "auth_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_COMPLETED = "profile_completed"
        private const val KEY_COUPLE_CONNECTED = "couple_connected"
        private const val KEY_COUPLE_ID = "couple_id"
    }
}
