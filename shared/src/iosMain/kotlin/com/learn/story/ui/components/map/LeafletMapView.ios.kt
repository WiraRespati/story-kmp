package com.learn.story.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.CoreGraphics.CGRect
import platform.Foundation.NSURL
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@Serializable
private data class LeafletBridgePayload(
    val action: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val id: String? = null
)

private class LeafletIosBridge(
    var onLocationPicked: ((Double, Double) -> Unit)? = null,
    var onMarkerClick: ((String) -> Unit)? = null
) : NSObject(), WKScriptMessageHandlerProtocol {
    private val json = Json { ignoreUnknownKeys = true }

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        val bodyString = didReceiveScriptMessage.body as? String ?: return
        try {
            val payload = json.decodeFromString<LeafletBridgePayload>(bodyString)
            when (payload.action) {
                "onLocationSelected" -> {
                    val lat = payload.lat
                    val lon = payload.lon
                    if (lat != null && lon != null) {
                        onLocationPicked?.invoke(lat, lon)
                    }
                }
                "onMarkerClicked" -> {
                    val id = payload.id
                    if (id != null) {
                        onMarkerClick?.invoke(id)
                    }
                }
            }
        } catch (e: Exception) {
            println("LeafletMap error: ${e.message}")
        }
    }
}

private class LeafletNavDelegate(
    private val onPageFinished: () -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onPageFinished()
    }
}

private const val MAP_BASE_URL = "https://story.learn.com/"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LeafletMapView(
    modifier: Modifier,
    initialLat: Double,
    initialLon: Double,
    zoom: Int,
    mode: MapInteractionMode,
    markers: List<MapMarker>,
    selectedLocation: Pair<Double, Double>?,
    onLocationPicked: ((Double, Double) -> Unit)?,
    onMarkerClick: ((String) -> Unit)?
) {
    val bridge = remember { LeafletIosBridge() }
    bridge.onLocationPicked = onLocationPicked
    bridge.onMarkerClick = onMarkerClick

    var webViewInstance by remember { mutableStateOf<WKWebView?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    val targetLat = selectedLocation?.first ?: initialLat
    val targetLon = selectedLocation?.second ?: initialLon

    val htmlContent = remember(mode) {
        generateLeafletHtml(
            initialLat = targetLat,
            initialLon = targetLon,
            zoom = zoom,
            mode = mode,
            markers = markers,
            selectedLocation = selectedLocation
        )
    }

    // Reactive marker update when markers list changes in VIEW_ONLY mode
    LaunchedEffect(markers, isLoaded, webViewInstance) {
        if (mode == MapInteractionMode.VIEW_ONLY && isLoaded && webViewInstance != null) {
            val markersJson = buildMarkersJson(markers)
            webViewInstance?.evaluateJavaScript(
                "if (typeof setMarkers === 'function') { setMarkers($markersJson); }",
                completionHandler = null
            )
        }
    }

    // Reactive picker update when selectedLocation changes in PICK_LOCATION mode
    LaunchedEffect(selectedLocation, isLoaded, webViewInstance) {
        if (isLoaded && webViewInstance != null && selectedLocation != null) {
            val (lat, lon) = selectedLocation
            webViewInstance?.evaluateJavaScript(
                "if (typeof updatePickerMarker === 'function') { updatePickerMarker($lat, $lon); }",
                completionHandler = null
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.configuration?.userContentController?.removeScriptMessageHandlerForName("storyBridge")
        }
    }

    UIKitView(
        factory = {
            val contentController = WKUserContentController().apply {
                addScriptMessageHandler(bridge, name = "storyBridge")
            }
            val configuration = WKWebViewConfiguration().apply {
                userContentController = contentController
            }
            val navDelegate = LeafletNavDelegate {
                isLoaded = true
                webViewInstance?.evaluateJavaScript(
                    "if (typeof onViewportResize === 'function') { onViewportResize(); }",
                    completionHandler = null
                )
                if (mode == MapInteractionMode.VIEW_ONLY) {
                    val markersJson = buildMarkersJson(markers)
                    webViewInstance?.evaluateJavaScript(
                        "if (typeof setMarkers === 'function') { setMarkers($markersJson); }",
                        completionHandler = null
                    )
                }
            }

            WKWebView(frame = cValue<CGRect>(), configuration = configuration).apply {
                navigationDelegate = navDelegate
                loadHTMLString(htmlContent, baseURL = NSURL.URLWithString(MAP_BASE_URL))
                webViewInstance = this
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { webView ->
            webViewInstance = webView
        }
    )
}
