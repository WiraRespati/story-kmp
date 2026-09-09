package com.learn.story.data.repository

import com.learn.story.data.remote.GeocodingPlace
import com.learn.story.data.remote.GeocodingService

interface LocationRepository {
    suspend fun reverseGeocode(lat: Double, lon: Double): String?
    suspend fun searchPlaces(query: String): List<GeocodingPlace>
}

fun LocationRepository(geocodingService: GeocodingService): LocationRepository = LocationRepositoryImpl(geocodingService)

class LocationRepositoryImpl(
    private val geocodingService: GeocodingService
) : LocationRepository {
    override suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return geocodingService.reverseGeocode(lat, lon)
    }

    override suspend fun searchPlaces(query: String): List<GeocodingPlace> {
        return geocodingService.searchPlaces(query)
    }
}
