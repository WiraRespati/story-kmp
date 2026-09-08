package com.learn.story.data.storage

import com.russhwolf.settings.Settings

class TokenStorage(
    private val settings: Settings = Settings()
) {
    fun saveAuth(token: String, userId: String, name: String) {
        settings.putString(KEY_TOKEN, token)
        settings.putString(KEY_USER_ID, userId)
        settings.putString(KEY_USER_NAME, name)
    }

    fun getToken(): String? = settings.getStringOrNull(KEY_TOKEN)

    fun getUserName(): String? = settings.getStringOrNull(KEY_USER_NAME)

    fun clear() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NAME)
    }

    fun isLoggedIn(): Boolean {
        val token = getToken()
        return !token.isNullOrEmpty() && token.trim().isNotEmpty()
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
    }
}
