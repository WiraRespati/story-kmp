package com.learn.story.data.repository

import com.learn.story.data.network.ApiResult
import com.learn.story.data.storage.TokenStorage
import com.learn.story.data.model.LoginRequest
import com.learn.story.data.model.RegisterRequest
import com.learn.story.data.model.UserModel
import com.learn.story.data.remote.StoryApiService

class AuthRepository(
    private val apiService: StoryApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun register(name: String, email: String, password: String): ApiResult<String> {
        return try {
            val response = apiService.register(
                RegisterRequest(name = name, email = email, password = password)
            )
            if (!response.error) {
                ApiResult.Success(response.message)
            } else {
                val errorMsg = if (response.message.trim().isNotEmpty()) response.message else "Registration failed"
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to register")
        }
    }

    suspend fun login(email: String, password: String): ApiResult<UserModel> {
        return try {
            val response = apiService.login(
                LoginRequest(email = email, password = password)
            )
            if (!response.error && response.loginResult != null) {
                val result = response.loginResult
                tokenStorage.saveAuth(
                    token = result.token,
                    userId = result.userId,
                    name = result.name
                )
                ApiResult.Success(
                    UserModel(
                        userId = result.userId,
                        name = result.name,
                        token = result.token
                    )
                )
            } else {
                val errorMsg = if (response.message.trim().isNotEmpty()) response.message else "Login failed"
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to login")
        }
    }

    suspend fun logout() {
        tokenStorage.clear()
    }

    fun isUserLoggedIn(): Boolean = tokenStorage.isLoggedIn()

    fun getUserName(): String? = tokenStorage.getUserName()
}
