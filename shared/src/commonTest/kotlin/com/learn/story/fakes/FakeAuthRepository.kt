package com.learn.story.fakes

import com.learn.story.data.model.UserModel
import com.learn.story.data.network.ApiResult
import com.learn.story.data.repository.AuthRepository

class FakeAuthRepository(
    var loginResult: ApiResult<UserModel> = ApiResult.Success(UserModel("user-1", "Test User", "dummy-token")),
    var registerResult: ApiResult<String> = ApiResult.Success("User created"),
    var initialIsLoggedIn: Boolean = false,
    var initialUserName: String? = "Test User"
) : AuthRepository {

    var loggedIn: Boolean = initialIsLoggedIn
    var currentUserName: String? = initialUserName
    var logoutCalled: Boolean = false

    override suspend fun register(name: String, email: String, password: String): ApiResult<String> {
        return registerResult
    }

    override suspend fun login(email: String, password: String): ApiResult<UserModel> {
        if (loginResult is ApiResult.Success) {
            loggedIn = true
            currentUserName = (loginResult as ApiResult.Success<UserModel>).data.name
        }
        return loginResult
    }

    override suspend fun logout() {
        logoutCalled = true
        loggedIn = false
        currentUserName = null
    }

    override fun isUserLoggedIn(): Boolean = loggedIn

    override fun getUserName(): String? = currentUserName
}
