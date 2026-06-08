package com.easyhooon.routepeek

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutePeekWebViewCaptureTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val capturedUrls = LinkedBlockingQueue<String>()
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        runOnMain {
            webView = WebView(instrumentation.targetContext).apply {
                settings.javaScriptEnabled = true
                RoutePeek.install(
                    webView = this,
                    onUrlChanged = { url -> capturedUrls.offer(url) },
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        RoutePeek.injectRouteListener(view)
                    }
                }
            }
        }
    }

    @After
    fun tearDown() {
        runOnMain {
            webView.destroy()
        }
    }

    @Test
    fun capturesInitialAndSpaRouteChangesFromWebView() {
        runOnMain {
            webView.loadDataWithBaseURL(
                "https://routepeek.test/start",
                "<!doctype html><html><body>RoutePeek</body></html>",
                "text/html",
                "UTF-8",
                null,
            )
        }

        assertNextCapturedUrl("https://routepeek.test/start")

        runJavaScript("history.pushState({}, '', '/pushed?tab=1#details');")
        assertNextCapturedUrl("https://routepeek.test/pushed?tab=1#details")

        runJavaScript("history.replaceState({}, '', '/replaced');")
        assertNextCapturedUrl("https://routepeek.test/replaced")

        runJavaScript("window.location.hash = 'section';")
        assertNextCapturedUrl("https://routepeek.test/replaced#section")
    }

    private fun runJavaScript(script: String) {
        runOnMain {
            webView.evaluateJavascript(script, null)
        }
    }

    private fun assertNextCapturedUrl(expectedUrl: String) {
        val deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        val seenUrls = mutableListOf<String>()

        while (System.nanoTime() < deadlineNanos) {
            val remainingNanos = deadlineNanos - System.nanoTime()
            val nextUrl = capturedUrls.poll(remainingNanos, TimeUnit.NANOSECONDS) ?: break
            seenUrls += nextUrl
            if (nextUrl == expectedUrl) {
                return
            }
        }

        fail("Expected to capture $expectedUrl, but saw $seenUrls")
    }

    private fun runOnMain(block: () -> Unit) {
        instrumentation.runOnMainSync(block)
    }
}
