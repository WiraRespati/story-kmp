package com.learn.story.fakes

import com.learn.story.data.remote.GeocodingPlace
import com.learn.story.data.repository.LocationRepository

class FakeLocationRepository(
    var geocodeAddress: String? = "Jakarta, Indonesia",
    var placesResult: List<GeocodingPlace> = emptyList()
) : LocationRepository {

    override suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return geocodeAddress
    }

    override suspend fun searchPlaces(query: String): List<GeocodingPlace> {
        return placesResult
    }
}
