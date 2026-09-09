package com.learn.story.data.remote

import com.learn.story.data.model.LoginRequest
import com.learn.story.data.model.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StoryApiServiceIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun createTestClient(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(this@StoryApiServiceIntegrationTest.json)
            }
            defaultRequest {
                url("https://story-api.dicoding.dev/v1/")
            }
        }
    }

    @Test
    fun testLogin_success_deserializesValidResponse() = runTest {
        val mockResponseJson = """
            {
                "error": false,
                "message": "success",
                "loginResult": {
                    "userId": "user-integration-1",
                    "name": "Integration User",
                    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy"
                }
            }
        """.trimIndent()

        val engine = MockEngine { request ->
            assertEquals("https://story-api.dicoding.dev/v1/login", request.url.toString())
            respond(
                content = mockResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(engine)
        val apiService = StoryApiService(client)

        val response = apiService.login(LoginRequest("test@example.com", "password123"))

        assertFalse(response.error)
        assertEquals("success", response.message)
        assertNotNull(response.loginResult)
        assertEquals("user-integration-1", response.loginResult.userId)
        assertEquals("Integration User", response.loginResult.name)
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy", response.loginResult.token)
    }

    @Test
    fun testLogin_unauthorized_handlesErrorResponseGracefully() = runTest {
        val mockErrorJson = """
            {
                "error": true,
                "message": "User not found"
            }
        """.trimIndent()

        val engine = MockEngine {
            respond(
                content = mockErrorJson,
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(engine)
        val apiService = StoryApiService(client)

        val response = apiService.login(LoginRequest("unknown@example.com", "wrongpass"))

        assertTrue(response.error)
        assertEquals("User not found", response.message)
    }

    @Test
    fun testRegister_success_sendsJsonBodyAndParsesGenericResponse() = runTest {
        val mockResponseJson = """
            {
                "error": false,
                "message": "User Created"
            }
        """.trimIndent()

        val engine = MockEngine { request ->
            assertEquals("https://story-api.dicoding.dev/v1/register", request.url.toString())
            respond(
                content = mockResponseJson,
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(engine)
        val apiService = StoryApiService(client)

        val response = apiService.register(RegisterRequest("Budi", "budi@test.com", "password123"))

        assertFalse(response.error)
        assertEquals("User Created", response.message)
    }

    @Test
    fun testGetStories_withQueryParams_serializesParametersAndDeserializesList() = runTest {
        val mockResponseJson = """
            {
                "error": false,
                "message": "Stories fetched successfully",
                "listStory": [
                    {
                        "id": "story-kmp-1",
                        "name": "KMP Traveller",
                        "description": "Indahnya pantai di Pulau Bali",
                        "photoUrl": "https://story-api.dicoding.dev/images/stories/photos-1.jpg",
                        "createdAt": "2024-03-01T12:00:00.000Z",
                        "lat": -8.7185,
                        "lon": 115.1686
                    }
                ]
            }
        """.trimIndent()

        var capturedUrl = ""
        val engine = MockEngine { request ->
            capturedUrl = request.url.toString()
            respond(
                content = mockResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(engine)
        val apiService = StoryApiService(client)

        val response = apiService.getStories(page = 2, size = 15, location = 1)

        assertTrue(capturedUrl.contains("page=2"))
        assertTrue(capturedUrl.contains("size=15"))
        assertTrue(capturedUrl.contains("location=1"))

        assertFalse(response.error)
        assertEquals(1, response.listStory.size)
        val story = response.listStory.first()
        assertEquals("story-kmp-1", story.id)
        assertEquals("KMP Traveller", story.name)
        assertEquals(-8.7185, story.lat)
        assertEquals(115.1686, story.lon)
    }

    @Test
    fun testGetStoryDetail_success_deserializesStoryDetailResponse() = runTest {
        val mockResponseJson = """
            {
                "error": false,
                "message": "Story found",
                "story": {
                    "id": "story-detail-99",
                    "name": "Detail Explorer",
                    "description": "Deskripsi detail lengkap",
                    "photoUrl": "https://example.com/detail.jpg",
                    "createdAt": "2024-03-02T10:00:00.000Z",
                    "lat": -6.9175,
                    "lon": 107.6191
                }
            }
        """.trimIndent()

        val engine = MockEngine { request ->
            assertEquals("https://story-api.dicoding.dev/v1/stories/story-detail-99", request.url.toString())
            respond(
                content = mockResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(engine)
        val apiService = StoryApiService(client)

        val response = apiService.getStoryDetail("story-detail-99")

        assertFalse(response.error)
        assertNotNull(response.story)
        assertEquals("story-detail-99", response.story.id)
        assertEquals("Detail Explorer", response.story.name)
        assertEquals(-6.9175, response.story.lat)
    }

    @Test
    fun testNetworkFailure_catchesExceptionAndReturnsSafeError() = runTest {
        val engine = MockEngine {
            throw io.ktor.client.network.sockets.ConnectTimeoutException("Koneksi timeout ke server", null)
        }

        val client = createTestClient(engine)
        val apiService = StoryApiService(client)

        val response = apiService.getStories()

        assertTrue(response.error)
        assertTrue(response.message.contains("Koneksi") || response.message.isNotEmpty())
    }
}
