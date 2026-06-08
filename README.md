# RoutePeek

RoutePeek is a tiny Android library for inspecting the current route inside a WebView.

It listens to WebView navigation and SPA-style route changes such as `pushState`, `replaceState`, `popstate`, and `hashchange`, then lets your app show the current route through a Compose overlay. The library only provides the WebView hook API and the Compose overlay API. It does not create or manage a floating window automatically, so the host app decides when and where to show it.

The overlay displays the route portion on screen to keep the UI compact. Copy and share actions still use the full URL, including the domain.
Long-press the collapsed overlay button and drag it into the bottom-center delete target to hide the overlay.

## Modules

- `routepeek`: debug implementation with WebView route tracking and Compose overlay UI
- `routepeek-noop`: no-op implementation with the same API surface for release builds
- `sample`: simple WebView sample app for checking route changes

## Setup

RoutePeek is not published yet. During local development, include the modules directly:

```kotlin
dependencies {
    debugImplementation(project(":routepeek"))
    releaseImplementation(project(":routepeek-noop"))
}
```

After publishing, use the Maven artifacts:

```kotlin
dependencies {
    debugImplementation("io.github.easyhooon:routepeek:<latest-version>")
    releaseImplementation("io.github.easyhooon:routepeek-noop:<latest-version>")
}
```

For custom build types, wire the real implementation only where you want RoutePeek enabled:

```kotlin
dependencies {
    debugImplementation("io.github.easyhooon:routepeek:<latest-version>")
    stagingImplementation("io.github.easyhooon:routepeek-noop:<latest-version>")
    releaseImplementation("io.github.easyhooon:routepeek-noop:<latest-version>")
}
```

## Usage

Install the WebView hook and keep the latest URL in your app state:

```kotlin
WebView(context).apply {
    settings.javaScriptEnabled = true
    RoutePeek.install(
        webView = this,
        onUrlChanged = { url -> currentUrl = url },
    )
}
```

Inject the route listener after the page is loaded:

```kotlin
override fun onPageFinished(view: WebView?, url: String?) {
    RoutePeek.injectRouteListener(view)
}
```

Place the overlay wherever it fits your screen:

```kotlin
Box(Modifier.fillMaxSize()) {
    AndroidView(...)

    RoutePeekOverlay(
        url = currentUrl,
        collapsedModifier = Modifier.align(Alignment.CenterEnd),
        expandedModifier = Modifier.align(Alignment.TopCenter),
    )
}
```

## License

Apache License 2.0
