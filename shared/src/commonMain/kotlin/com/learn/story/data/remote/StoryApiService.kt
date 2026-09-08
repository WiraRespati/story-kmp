package com.learn.story.data.remote

import com.learn.story.data.model.GenericResponse
import com.learn.story.data.model.LoginRequest
import com.learn.story.data.model.LoginResponse
import com.learn.story.data.model.RegisterRequest
import com.learn.story.data.model.StoriesResponse
import com.learn.story.data.model.StoryDetailResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class StoryApiService(
    private val client: HttpClient
) {
    suspend fun register(request: RegisterRequest): GenericResponse {
        val response = client.post("register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return try {
            response.body<GenericResponse>()
        } catch (_: Exception) {
            GenericResponse(
                error = true,
                message = "HTTP ${response.status.value}: ${response.status.description}"
            )
        }
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = client.post("login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return try {
            response.body<LoginResponse>()
        } catch (_: Exception) {
            LoginResponse(
                error = true,
                message = "HTTP ${response.status.value}: ${response.status.description}"
            )
        }
    }

    suspend fun getStories(page: Int? = null, size: Int? = null, location: Int? = null): StoriesResponse {
        val response = client.get("stories") {
            url {
                page?.let { parameters.append("page", it.toString()) }
                size?.let { parameters.append("size", it.toString()) }
                location?.let { parameters.append("location", it.toString()) }
            }
        }
        return try {
            response.body<StoriesResponse>()
        } catch (_: Exception) {
            StoriesResponse(
                error = true,
                message = "HTTP ${response.status.value}: ${response.status.description}"
            )
        }
    }

    suspend fun getStoryDetail(id: String): StoryDetailResponse {
        val response = client.get("stories/$id")
        return try {
            response.body<StoryDetailResponse>()
        } catch (_: Exception) {
            StoryDetailResponse(
                error = true,
                message = "HTTP ${response.status.value}: ${response.status.description}"
            )
        }
    }

    suspend fun addStory(
        description: String,
        photoBytes: ByteArray,
        fileName: String = "story.jpg",
        lat: Double? = null,
        lon: Double? = null
    ): GenericResponse {
        val response = client.submitFormWithBinaryData(
            url = "stories",
            formData = formData {
                append("description", description)
                append("photo", photoBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
                lat?.let { append("lat", it.toString()) }
                lon?.let { append("lon", it.toString()) }
            }
        )
        return try {
            response.body<GenericResponse>()
        } catch (_: Exception) {
            GenericResponse(
                error = true,
                message = "HTTP ${response.status.value}: ${response.status.description}"
            )
        }
    }

    suspend fun addGuestStory(
        description: String,
        photoBytes: ByteArray,
        fileName: String = "story.jpg",
        lat: Double? = null,
        lon: Double? = null
    ): GenericResponse {
        val response = client.submitFormWithBinaryData(
            url = "stories/guest",
            formData = formData {
                append("description", description)
                append("photo", photoBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
                lat?.let { append("lat", it.toString()) }
                lon?.let { append("lon", it.toString()) }
            }
        )
        return try {
            response.body<GenericResponse>()
        } catch (_: Exception) {
            GenericResponse(
                error = true,
                message = "HTTP ${response.status.value}: ${response.status.description}"
            )
        }
    }
}
