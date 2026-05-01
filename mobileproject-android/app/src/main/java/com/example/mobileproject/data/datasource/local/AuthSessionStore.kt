package com.example.mobileproject.data.datasource.local

import android.content.Context
import com.example.mobileproject.domain.entity.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    fun load(): AuthSession? {
        val token = preferences.getString(KEY_TOKEN, null)?.trim().orEmpty()
        if (token.isBlank()) {
            return null
        }

        return AuthSession(
            token = token,
            username = preferences.getString(KEY_USERNAME, "").orEmpty(),
            email = preferences.getString(KEY_EMAIL, "").orEmpty(),
            profileCompleted = preferences.getBoolean(KEY_PROFILE_COMPLETED, false),
            coupleConnected = preferences.getBoolean(KEY_COUPLE_CONNECTED, false),
            coupleId = preferences.getString(KEY_COUPLE_ID, null)
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

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
