package com.learn.story.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimAddress(
    @SerialName("city") val city: String? = null,
    @SerialName("town") val town: String? = null,
    @SerialName("suburb") val suburb: String? = null,
    @SerialName("village") val village: String? = null,
    @SerialName("county") val county: String? = null,
    @SerialName("state") val state: String? = null,
    @SerialName("country") val country: String? = null
)

@Serializable
data class NominatimReverseResponse(
    @SerialName("display_name") val displayName: String = "",
    @SerialName("address") val address: NominatimAddress? = null
)

@Serializable
data class NominatimSearchResult(
    @SerialName("display_name") val displayName: String,
    @SerialName("lat") val lat: String,
    @SerialName("lon") val lon: String
)

data class GeocodingPlace(
    val displayName: String,
    val shortName: String,
    val lat: Double,
    val lon: Double
)

class GeocodingService(
    private val client: HttpClient
) {
    suspend fun reverseGeocode(lat: Double, lon: Double): String {
        return try {
            val response = client.get("https://nominatim.openstreetmap.org/reverse") {
                header("User-Agent", "StoryApp-Dicoding-KMP/1.0")
                url {
                    parameters.append("format", "json")
                    parameters.append("lat", lat.toString())
                    parameters.append("lon", lon.toString())
                    parameters.append("zoom", "14")
                }
            }.body<NominatimReverseResponse>()

            val addr = response.address
            val parts = listOfNotNull(
                addr?.suburb ?: addr?.village ?: addr?.town ?: addr?.city,
                addr?.county,
                addr?.state
            ).filter { it.isNotBlank() }

            if (parts.isNotEmpty()) {
                parts.joinToString(", ")
            } else if (response.displayName.isNotBlank()) {
                response.displayName.split(",").take(3).joinToString(",").trim()
            } else {
                val roundLat = (lat * 10000).toLong() / 10000.0
                val roundLon = (lon * 10000).toLong() / 10000.0
                "Lat: $roundLat, Lon: $roundLon"
            }
        } catch (_: Exception) {
            "Lat: $lat, Lon: $lon"
        }
    }

    suspend fun searchPlaces(query: String): List<GeocodingPlace> {
        if (query.trim().length < 3) return emptyList()
        return try {
            val list = client.get("https://nominatim.openstreetmap.org/search") {
                header("User-Agent", "StoryApp-Dicoding-KMP/1.0")
                url {
                    parameters.append("q", query.trim())
                    parameters.append("format", "json")
                    parameters.append("limit", "5")
                }
            }.body<List<NominatimSearchResult>>()

            list.mapNotNull { item ->
                val latVal = item.lat.toDoubleOrNull()
                val lonVal = item.lon.toDoubleOrNull()
                if (latVal != null && lonVal != null) {
                    val parts = item.displayName.split(",")
                    val short = parts.take(2).joinToString(", ").trim()
                    GeocodingPlace(
                        displayName = item.displayName,
                        shortName = if (short.isNotBlank()) short else item.displayName,
                        lat = latVal,
                        lon = lonVal
                    )
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
