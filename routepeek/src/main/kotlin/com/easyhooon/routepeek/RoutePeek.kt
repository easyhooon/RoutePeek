package com.easyhooon.routepeek

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

object RoutePeek {
    private const val JS_INTERFACE_NAME = "RoutePeek"
    val version: String = BuildConfig.ROUTEPEEK_VERSION

    @SuppressLint("JavascriptInterface")
    fun install(
        webView: WebView,
        onUrlChanged: (String) -> Unit,
    ) {
        webView.addJavascriptInterface(RouteBridge(onUrlChanged), JS_INTERFACE_NAME)
    }

    fun injectRouteListener(webView: WebView?) {
        webView?.evaluateJavascript(routeListenerScript, null)
    }

    private val routeListenerScript = """
        (function() {
          if (window.__routePeekInstalled === true) {
            if (typeof window.__routePeekNotify === 'function') {
              window.__routePeekNotify();
            }
            return true;
          }

          window.__routePeekInstalled = true;

          var notify = function() {
            try {
              if (window.$JS_INTERFACE_NAME && window.$JS_INTERFACE_NAME.onRouteChanged) {
                window.$JS_INTERFACE_NAME.onRouteChanged(window.location.href);
              }
            } catch (e) {
            }
          };

          window.__routePeekNotify = notify;

          var wrapHistory = function(name) {
            var original = window.history[name];
            if (typeof original !== 'function') {
              return;
            }

            window.history[name] = function() {
              var result = original.apply(this, arguments);
              notify();
              return result;
            };
          };

          wrapHistory('pushState');
          wrapHistory('replaceState');
          window.addEventListener('popstate', notify);
          window.addEventListener('hashchange', notify);
          notify();
          return true;
        })();
    """.trimIndent()

    private class RouteBridge(
        private val onUrlChanged: (String) -> Unit,
    ) {
        private val mainHandler = Handler(Looper.getMainLooper())

        @Volatile
        private var lastUrl: String? = null

        @JavascriptInterface
        fun onRouteChanged(url: String?) {
            val nextUrl = url?.takeIf { it.isNotBlank() } ?: return
            if (nextUrl == lastUrl) return

            lastUrl = nextUrl
            mainHandler.post {
                onUrlChanged(nextUrl)
            }
        }
    }
}

data class RoutePeekOverlayConfig(
    val maxPanelWidth: Dp = 420.dp,
    val copyLabel: String = "URL",
    val copyToastText: String = "URL이 복사되었습니다",
    val shareChooserTitle: String = "URL 공유",
    val removeTargetContentDescription: String = "RoutePeek 숨기기",
)

@Composable
fun RoutePeekOverlay(
    url: String,
    collapsedModifier: Modifier = Modifier,
    expandedModifier: Modifier = Modifier,
    config: RoutePeekOverlayConfig = RoutePeekOverlayConfig(),
) {
    if (url.isBlank()) return

    var expanded by rememberSaveable { mutableStateOf(false) }
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var panelOffsetX by rememberSaveable { mutableStateOf(0f) }
    var panelOffsetY by rememberSaveable { mutableStateOf(0f) }
    var collapsedOffsetX by rememberSaveable { mutableStateOf(0f) }
    var collapsedOffsetY by rememberSaveable { mutableStateOf(0f) }
    var collapsedDragging by remember { mutableStateOf(false) }
    var collapsedBounds by remember { mutableStateOf<Rect?>(null) }
    var removeTargetBounds by remember { mutableStateOf<Rect?>(null) }
    val currentCollapsedBounds by rememberUpdatedState(collapsedBounds)
    val currentRemoveTargetBounds by rememberUpdatedState(removeTargetBounds)
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val displayUrl = remember(url) { url.toRoutePeekDisplayRoute() }
    val collapsedOffsetModifier = Modifier.offset {
        IntOffset(
            x = collapsedOffsetX.roundToInt(),
            y = collapsedOffsetY.roundToInt(),
        )
    }
    val removeTargetActive = collapsedDragging &&
        isCollapsedInRemoveTarget(collapsedBounds, removeTargetBounds)
    val removeTargetSize = if (removeTargetActive) 76.dp else 64.dp
    val removeTargetContainerSize = if (removeTargetActive) 82.dp else 64.dp
    val removeTargetIconSize = if (removeTargetActive) 34.dp else 28.dp
    val collapsedDragModifier = Modifier.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                removeTargetBounds = null
                collapsedDragging = true
            },
            onDragCancel = {
                collapsedDragging = false
                collapsedOffsetX = 0f
                collapsedOffsetY = 0f
            },
            onDragEnd = {
                if (isCollapsedInRemoveTarget(currentCollapsedBounds, currentRemoveTargetBounds)) {
                    dismissed = true
                }
                collapsedDragging = false
                collapsedOffsetX = 0f
                collapsedOffsetY = 0f
            },
            onDrag = { change, dragAmount ->
                change.consume()
                collapsedOffsetX += dragAmount.x
                collapsedOffsetY += dragAmount.y
            },
        )
    }
    val panelOffsetModifier = Modifier.offset {
        IntOffset(
            x = panelOffsetX.roundToInt(),
            y = panelOffsetY.roundToInt(),
        )
    }
    val panelDragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                change.consume()
                panelOffsetX += dragAmount.x
                panelOffsetY += dragAmount.y
            },
        )
    }

    if (dismissed) return

    Box(modifier = Modifier.fillMaxSize()) {
        if (!expanded) {
            if (collapsedDragging) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(bottom = 28.dp)
                        .size(removeTargetContainerSize)
                        .onGloballyPositioned { coordinates ->
                            removeTargetBounds = coordinates.boundsInWindow()
                        }
                        .zIndex(9f),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.size(removeTargetSize),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = if (removeTargetActive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.94f)
                        },
                        contentColor = if (removeTargetActive) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        tonalElevation = if (removeTargetActive) 12.dp else 8.dp,
                        shadowElevation = 0.dp,
                        border = if (removeTargetActive) {
                            BorderStroke(3.dp, MaterialTheme.colorScheme.onError)
                        } else {
                            null
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                modifier = Modifier.size(removeTargetIconSize),
                                imageVector = Icons.Default.Delete,
                                contentDescription = config.removeTargetContentDescription,
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = collapsedModifier
                    .then(collapsedOffsetModifier)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .onGloballyPositioned { coordinates ->
                        collapsedBounds = coordinates.boundsInWindow()
                    }
                    .then(collapsedDragModifier)
                    .zIndex(10f),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
            ) {
                IconButton(
                    modifier = Modifier.size(42.dp),
                    onClick = { expanded = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "RoutePeek 열기",
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
            return@Box
        }

        Surface(
            modifier = expandedModifier
                .then(panelOffsetModifier)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .widthIn(max = config.maxPanelWidth)
                .zIndex(10f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.92f),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 6.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .then(panelDragModifier),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = displayUrl,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = {
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText(config.copyLabel, url)
                            clipboard.setClipEntry(ClipEntry(clipData))
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                Toast.makeText(context, config.copyToastText, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "URL 복사",
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }

                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, url)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, config.shareChooserTitle))
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "URL 공유",
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }

                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = { expanded = false },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "RoutePeek 접기",
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }
    }
}

private fun Rect.centerOffset(): Offset = Offset(
    x = (left + right) / 2f,
    y = (top + bottom) / 2f,
)

internal fun isCollapsedInRemoveTarget(
    collapsedBounds: Rect?,
    removeTargetBounds: Rect?,
): Boolean {
    val collapsedCenter = collapsedBounds?.centerOffset() ?: return false
    return removeTargetBounds?.contains(collapsedCenter) == true
}

private fun String.toRoutePeekDisplayRoute(): String {
    val uri = runCatching { Uri.parse(this) }.getOrNull() ?: return this
    val scheme = uri.scheme?.lowercase()
    if (scheme != "http" && scheme != "https") return this

    val path = uri.encodedPath?.takeIf { it.isNotBlank() } ?: "/"
    val query = uri.encodedQuery?.let { "?$it" }.orEmpty()
    val fragment = uri.encodedFragment?.let { "#$it" }.orEmpty()
    return path + query + fragment
}
