package com.learn.story.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfflineStoryDraft(
    @SerialName("id") val id: String,
    @SerialName("description") val description: String,
    @SerialName("photoBase64") val photoBase64: String,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lon") val lon: Double? = null,
    @SerialName("isGuest") val isGuest: Boolean = false,
    @SerialName("createdAtEpoch") val createdAtEpoch: Long,
    @SerialName("locationName") val locationName: String? = null,
    @SerialName("lastError") val lastError: String? = null
)
