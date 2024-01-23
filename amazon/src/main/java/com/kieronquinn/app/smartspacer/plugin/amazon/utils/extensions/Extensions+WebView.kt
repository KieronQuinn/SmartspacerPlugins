package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kieronquinn.app.smartspacer.plugin.amazon.HEADERS
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getDisplayPortraitHeight
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getDisplayPortraitWidth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun createWebView(
    context: Context,
    cookieManager: CookieManager,
    userAgent: String
) = WebView(context).apply {
    val displayWidth = context.getDisplayPortraitWidth()
    val displayHeight = context.getDisplayPortraitHeight()
    val width = View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.EXACTLY)
    val height = View.MeasureSpec.makeMeasureSpec(displayHeight, View.MeasureSpec.EXACTLY)
    measure(width, height)
    layout(0, 0, displayWidth, displayHeight)
    setup(cookieManager, userAgent)
}

fun WebView.executeJavascriptSync(js: String, callback: ((String) -> Unit)? = null) {
    evaluateJavascript(js) {
        callback?.invoke(it)
    }
}

suspend fun WebView.executeJavascript(js: String) = suspendCoroutine<String> {
    evaluateJavascript(js) { result ->
        it.resume(result)
    }
}

suspend fun WebView.getHtml(): String = suspendCoroutine {
    evaluateJavascript("(function(){return window.document.head.outerHTML + window.document.body.outerHTML})();") { html ->
        it.resume(html)
    }
}

fun WebView.onPageLoaded(
    onLoadStart: () -> Unit = {},
    onLoadFinished: () -> Unit = {},
    runOnLoad: suspend WebView.(url: String) -> Unit = {}
) = callbackFlow {
    val client = object: WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onLoadStart()
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            launch {
                val html = getHtml().loadHtml(url) ?: Document("")
                runOnLoad(view, url)
                trySend(html)
                onLoadFinished()
            }
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            view.loadUrl(request.url.toString(), request.requestHeaders)
            return true
        }
    }
    webViewClient = client
    awaitClose {
        webViewClient = object: WebViewClient() {}
    }
}

fun WebView.load(url: String) = callbackFlow {
    val client = object: WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            launch {
                val document = getHtml().loadHtml(url) ?: Document("")
                trySend(document)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            view.loadUrl(request.url.toString(), request.requestHeaders)
            return true
        }
    }
    loadUrl(url, HEADERS)
    webViewClient = client
    awaitClose {
        webViewClient = object: WebViewClient() {}
    }
}

fun WebView.setup(cookieManager: CookieManager, userAgent: String) {
    settings.apply {
        javaScriptEnabled = true
        userAgentString = userAgent
    }
    cookieManager.setAcceptThirdPartyCookies(this, true)
}

private fun String.loadHtml(url: String): Document? {
    return try {
        Parser.parse(StringEscapeUtils.unescapeJava(this), url)
    }catch (e: Exception) {
        null
    }
}