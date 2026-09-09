package com.learn.story.data.repository

import com.learn.story.data.remote.GeocodingPlace
import com.learn.story.data.remote.GeocodingService

class LocationRepository(
    private val geocodingService: GeocodingService
) {
    suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return geocodingService.reverseGeocode(lat, lon)
    }

    suspend fun searchPlaces(query: String): List<GeocodingPlace> {
        return geocodingService.searchPlaces(query)
    }
}
