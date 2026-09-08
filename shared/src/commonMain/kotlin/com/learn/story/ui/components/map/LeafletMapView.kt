package com.learn.story.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class MapMarker(
    val id: String,
    val title: String,
    val snippet: String = "",
    val lat: Double,
    val lon: Double,
    val photoUrl: String? = null
)

enum class MapInteractionMode {
    VIEW_ONLY,
    PICK_LOCATION
}

@Composable
expect fun LeafletMapView(
    modifier: Modifier = Modifier,
    initialLat: Double = -6.2088,
    initialLon: Double = 106.8456,
    zoom: Int = 12,
    mode: MapInteractionMode = MapInteractionMode.VIEW_ONLY,
    markers: List<MapMarker> = emptyList(),
    selectedLocation: Pair<Double, Double>? = null,
    onLocationPicked: ((Double, Double) -> Unit)? = null,
    onMarkerClick: ((String) -> Unit)? = null
)
