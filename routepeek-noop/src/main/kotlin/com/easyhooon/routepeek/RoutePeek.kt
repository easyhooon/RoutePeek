package com.easyhooon.routepeek

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object RoutePeek {
    val version: String = BuildConfig.ROUTEPEEK_VERSION

    fun install(
        webView: WebView,
        onUrlChanged: (String) -> Unit,
    ) = Unit

    fun injectRouteListener(webView: WebView?) = Unit
}

data class RoutePeekOverlayConfig(
    val maxPanelWidth: Dp = 420.dp,
    val copyLabel: String = "URL",
    val copyToastText: String = "URL이 복사되었습니다",
    val shareChooserTitle: String = "URL 공유",
)

@Composable
fun RoutePeekOverlay(
    url: String,
    collapsedModifier: Modifier = Modifier,
    expandedModifier: Modifier = Modifier,
    config: RoutePeekOverlayConfig = RoutePeekOverlayConfig(),
) = Unit
