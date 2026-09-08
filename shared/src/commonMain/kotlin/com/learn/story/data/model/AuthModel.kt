package com.learn.story.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class GenericResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String
)

@Serializable
data class LoginResult(
    @SerialName("userId") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("token") val token: String
)

@Serializable
data class LoginResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String,
    @SerialName("loginResult") val loginResult: LoginResult? = null
)

data class UserModel(
    val userId: String,
    val name: String,
    val token: String
)
