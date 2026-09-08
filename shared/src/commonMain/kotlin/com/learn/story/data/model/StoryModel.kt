package com.learn.story.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Story(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("photoUrl") val photoUrl: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lon") val lon: Double? = null
)

@Serializable
data class StoriesResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String,
    @SerialName("listStory") val listStory: List<Story> = emptyList()
)

@Serializable
data class StoryDetailResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("message") val message: String,
    @SerialName("story") val story: Story? = null
)
