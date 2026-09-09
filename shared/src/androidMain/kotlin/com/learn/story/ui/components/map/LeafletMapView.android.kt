package com.learn.story.ui.components.map

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

class LeafletBridge(
    var onLocationPicked: ((Double, Double) -> Unit)? = null,
    var onMarkerClick: ((String) -> Unit)? = null
) {
    @JavascriptInterface
    fun onLocationSelected(lat: Double, lon: Double) {
        onLocationPicked?.invoke(lat, lon)
    }

    @JavascriptInterface
    fun onMarkerClicked(id: String) {
        onMarkerClick?.invoke(id)
    }
}

private const val MAP_BASE_URL = "https://tile.openstreetmap.org/"

@SuppressLint("SetJavaScriptEnabled")
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
    val bridge = remember { LeafletBridge() }
    bridge.onLocationPicked = onLocationPicked
    bridge.onMarkerClick = onMarkerClick

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webViewRecreateKey by remember { mutableStateOf(0) }

    val targetLat = selectedLocation?.first ?: initialLat
    val targetLon = selectedLocation?.second ?: initialLon

    val htmlContent = remember(mode, targetLat, targetLon, zoom) {
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
    LaunchedEffect(markers, webViewInstance) {
        if (mode == MapInteractionMode.VIEW_ONLY && webViewInstance != null) {
            val markersJson = buildMarkersJson(markers)
            webViewInstance?.evaluateJavascript(
                "if (typeof setMarkers === 'function') { setMarkers($markersJson); }",
                null
            )
        }
    }

    // Reactive picker update when selectedLocation changes in PICK_LOCATION mode
    LaunchedEffect(selectedLocation, webViewInstance) {
        selectedLocation?.let { (lat, lon) ->
            webViewInstance?.evaluateJavascript(
                "if (typeof updatePickerMarker === 'function') { updatePickerMarker($lat, $lon); }",
                null
            )
        }
    }

    key(webViewRecreateKey) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.userAgentString = "${settings.userAgentString} StoryApp/1.0"
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    // Listen for View layout passes to invalidate and fit Leaflet correctly
                    addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                        val w = right - left
                        val h = bottom - top
                        val oldW = oldRight - oldLeft
                        val oldH = oldBottom - oldTop
                        if (w > 0 && h > 0 && (w != oldW || h != oldH)) {
                            post {
                                evaluateJavascript(
                                    "if (typeof onViewportResize === 'function') { onViewportResize(); }",
                                    null
                                )
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                            android.util.Log.i(
                                "LeafletMap",
                                "Console: ${consoleMessage?.message()} [line ${consoleMessage?.lineNumber()}]"
                            )
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.post {
                                view.evaluateJavascript(
                                    "if (typeof onViewportResize === 'function') { onViewportResize(); }",
                                    null
                                )
                                if (mode == MapInteractionMode.VIEW_ONLY) {
                                    val markersJson = buildMarkersJson(markers)
                                    view.evaluateJavascript(
                                        "if (typeof setMarkers === 'function') { setMarkers($markersJson); }",
                                        null
                                    )
                                }
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            android.util.Log.e("LeafletMap", "Load error: ${request?.url} -> ${error?.description}")
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url?.toString() ?: ""
                            if (url.endsWith("favicon.ico") || url.contains("/favicon.ico")) {
                                return WebResourceResponse("image/x-icon", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: RenderProcessGoneDetail?
                        ): Boolean {
                            android.util.Log.w("LeafletMap", "onRenderProcessGone detected (didCrash=${detail?.didCrash()}). Recovering...")
                            view?.let {
                                val parent = it.parent as? ViewGroup
                                parent?.removeView(it)
                                it.destroy()
                            }
                            webViewInstance = null
                            webViewRecreateKey++
                            return true
                        }
                    }

                    addJavascriptInterface(bridge, "AndroidBridge")
                    loadDataWithBaseURL(MAP_BASE_URL, htmlContent, "text/html", "UTF-8", null)
                    webViewInstance = this
                }
            },
            update = { webView ->
                webViewInstance = webView
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
            }
        )
    }
}

