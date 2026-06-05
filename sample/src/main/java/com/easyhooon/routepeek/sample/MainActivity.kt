package com.easyhooon.routepeek.sample

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.easyhooon.routepeek.RoutePeek
import com.easyhooon.routepeek.RoutePeekOverlay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                RoutePeekSampleScreen()
            }
        }
    }
}

private const val SampleBaseUrl = "https://routepeek.sample/"

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun RoutePeekSampleScreen() {
    var currentUrl by remember { mutableStateOf(SampleBaseUrl) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    RoutePeek.install(
                        webView = this,
                        onUrlChanged = { currentUrl = it },
                    )
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            url?.let { currentUrl = it }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url?.let { currentUrl = it }
                            RoutePeek.injectRouteListener(view)
                        }
                    }
                    val sampleHtml = context.assets.open("routepeek_sample.html")
                        .bufferedReader()
                        .use { it.readText() }
                    loadDataWithBaseURL(
                        SampleBaseUrl,
                        sampleHtml,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                }
            },
        )

        RoutePeekOverlay(
            url = currentUrl,
            collapsedModifier = Modifier.align(Alignment.CenterEnd),
            expandedModifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
