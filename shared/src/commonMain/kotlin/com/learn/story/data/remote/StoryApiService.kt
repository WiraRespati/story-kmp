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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class StoryApiService(
    private val client: HttpClient
) {
    private suspend inline fun <T> safeRequest(
        crossinline call: suspend () -> HttpResponse,
        crossinline parse: suspend (HttpResponse) -> T,
        crossinline onError: (String) -> T
    ): T {
        return try {
            val response = call()
            try {
                parse(response)
            } catch (_: Exception) {
                onError("HTTP ${response.status.value}: ${response.status.description}")
            }
        } catch (e: Exception) {
            onError(e.message ?: "Koneksi ke server gagal")
        }
    }

    suspend fun register(request: RegisterRequest): GenericResponse = safeRequest(
        call = {
            client.post("register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        },
        parse = { it.body<GenericResponse>() },
        onError = { GenericResponse(error = true, message = it) }
    )

    suspend fun login(request: LoginRequest): LoginResponse = safeRequest(
        call = {
            client.post("login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        },
        parse = { it.body<LoginResponse>() },
        onError = { LoginResponse(error = true, message = it) }
    )

    suspend fun getStories(page: Int? = null, size: Int? = null, location: Int? = null): StoriesResponse = safeRequest(
        call = {
            client.get("stories") {
                url {
                    page?.let { parameters.append("page", it.toString()) }
                    size?.let { parameters.append("size", it.toString()) }
                    location?.let { parameters.append("location", it.toString()) }
                }
            }
        },
        parse = { it.body<StoriesResponse>() },
        onError = { StoriesResponse(error = true, message = it) }
    )

    suspend fun getStoryDetail(id: String): StoryDetailResponse = safeRequest(
        call = { client.get("stories/$id") },
        parse = { it.body<StoryDetailResponse>() },
        onError = { StoryDetailResponse(error = true, message = it) }
    )

    suspend fun addStory(
        description: String,
        photoBytes: ByteArray,
        fileName: String = "story.jpg",
        lat: Double? = null,
        lon: Double? = null
    ): GenericResponse = safeRequest(
        call = {
            client.submitFormWithBinaryData(
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
        },
        parse = { it.body<GenericResponse>() },
        onError = { GenericResponse(error = true, message = it) }
    )

    suspend fun addGuestStory(
        description: String,
        photoBytes: ByteArray,
        fileName: String = "story.jpg",
        lat: Double? = null,
        lon: Double? = null
    ): GenericResponse = safeRequest(
        call = {
            client.submitFormWithBinaryData(
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
        },
        parse = { it.body<GenericResponse>() },
        onError = { GenericResponse(error = true, message = it) }
    )
}
