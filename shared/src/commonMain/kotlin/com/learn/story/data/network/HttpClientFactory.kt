package com.learn.story.data.network

import com.learn.story.BuildKonfig
import com.learn.story.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine, tokenStorage: TokenStorage): HttpClient {
    return HttpClient(engine) {
        defaultRequest {
            url(BuildKonfig.BASE_URL)
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.DEFAULT
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenStorage.getToken()
                    if (!token.isNullOrEmpty()) BearerTokens(token, "") else null
                }
                sendWithoutRequest { request ->
                    val path = request.url.encodedPath
                    !path.endsWith("login") && !path.endsWith("register")
                }
            }
        }
    }
}

fun createGeocodingHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
            logger = Logger.DEFAULT
        }
    }
}
