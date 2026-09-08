package com.learn.story.ui.components.map

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun buildMarkersJson(markers: List<MapMarker>): String {
    return buildJsonArray {
        markers.forEach { marker ->
            add(buildJsonObject {
                put("id", marker.id)
                put("title", marker.title)
                put("snippet", marker.snippet)
                put("lat", marker.lat)
                put("lon", marker.lon)
            })
        }
    }.toString()
}

fun generateLeafletHtml(
    initialLat: Double,
    initialLon: Double,
    zoom: Int,
    mode: MapInteractionMode,
    markers: List<MapMarker>,
    selectedLocation: Pair<Double, Double>?
): String {
    val initialMarkersJson = buildMarkersJson(markers)
    val isPicker = mode == MapInteractionMode.PICK_LOCATION
    val pickerLat = selectedLocation?.first ?: initialLat
    val pickerLon = selectedLocation?.second ?: initialLon
    val hasInitialPicker = selectedLocation != null

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            overflow: hidden;
            background: #e2e8f0;
            -webkit-user-select: none;
            user-select: none;
            -webkit-touch-callout: none;
        }
        #map {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            width: 100%;
            height: 100%;
        }
        .leaflet-popup-content-wrapper {
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }
        .popup-title {
            font-weight: bold;
            font-size: 14px;
            margin-bottom: 4px;
            color: #0F172A;
        }
        .popup-desc {
            font-size: 12px;
            color: #475569;
            margin-bottom: 8px;
            line-height: 1.4;
        }
        .popup-btn {
            background: #3B82F6;
            color: white;
            border: none;
            border-radius: 6px;
            padding: 8px 12px;
            font-size: 13px;
            cursor: pointer;
            font-weight: 600;
            width: 100%;
            touch-action: manipulation;
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        // Leaflet default icon CDN
        delete L.Icon.Default.prototype._getIconUrl;
        L.Icon.Default.mergeOptions({
            iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
            iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
            shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png'
        });

        function escapeHtml(text) {
            if (!text) return '';
            return text
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }

        function sendBridgeMessage(payload) {
            // iOS WebKit MessageHandler
            if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.storyBridge) {
                window.webkit.messageHandlers.storyBridge.postMessage(JSON.stringify(payload));
            }
            // Android JavaScriptInterface
            if (window.AndroidBridge) {
                if (payload.action === 'onLocationSelected') {
                    window.AndroidBridge.onLocationSelected(payload.lat, payload.lon);
                } else if (payload.action === 'onMarkerClicked') {
                    window.AndroidBridge.onMarkerClicked(payload.id);
                }
            }
        }

        var map = L.map('map', { zoomControl: true }).setView([$initialLat, $initialLon], $zoom);
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(map);

        var isPicker = $isPicker;
        var pickerMarker = null;
        var markersLayer = L.layerGroup().addTo(map);

        function fitToMarkers() {
            var b = [];
            markersLayer.eachLayer(function(l) {
                if (l.getLatLng) b.push(l.getLatLng());
            });
            if (b.length > 1) {
                map.fitBounds(b, { padding: [40, 40], maxZoom: 15 });
            } else if (b.length === 1) {
                map.setView(b[0], 14);
            }
        }

        function onViewportResize() {
            if (typeof map !== 'undefined') {
                map.invalidateSize();
                if (!isPicker) {
                    fitToMarkers();
                } else if (pickerMarker) {
                    map.panTo(pickerMarker.getLatLng());
                }
            }
        }

        window.addEventListener('resize', onViewportResize);
        setTimeout(onViewportResize, 100);
        setTimeout(onViewportResize, 400);
        setTimeout(onViewportResize, 1000);

        function updatePickerMarker(lat, lon) {
            if (!pickerMarker) {
                pickerMarker = L.marker([lat, lon], { draggable: true }).addTo(map);
                pickerMarker.on('dragend', function(e) {
                    var pos = pickerMarker.getLatLng();
                    sendBridgeMessage({ action: 'onLocationSelected', lat: pos.lat, lon: pos.lng });
                });
            } else {
                pickerMarker.setLatLng([lat, lon]);
            }
            map.invalidateSize();
            map.setView([lat, lon], 14);
        }

        function setMarkers(markersData) {
            markersLayer.clearLayers();
            markersData.forEach(function(item) {
                var m = L.marker([item.lat, item.lon]).addTo(markersLayer);
                var popupHtml = '<div class="popup-title">' + escapeHtml(item.title) + '</div>' +
                                '<div class="popup-desc">' + escapeHtml(item.snippet) + '</div>' +
                                '<button class="popup-btn" onclick="onPopupClick(\'' + item.id + '\')">Lihat Story</button>';
                m.bindPopup(popupHtml);
            });

            setTimeout(function() {
                map.invalidateSize();
                fitToMarkers();
            }, 100);
        }

        if (isPicker) {
            ${if (hasInitialPicker) "updatePickerMarker($pickerLat, $pickerLon);" else ""}
            map.on('click', function(e) {
                updatePickerMarker(e.latlng.lat, e.latlng.lng);
                sendBridgeMessage({ action: 'onLocationSelected', lat: e.latlng.lat, lon: e.latlng.lng });
            });
        } else {
            var initialData = $initialMarkersJson;
            setMarkers(initialData);
        }

        function onPopupClick(id) {
            sendBridgeMessage({ action: 'onMarkerClicked', id: id });
        }
    </script>
</body>
</html>
    """.trimIndent()
}
