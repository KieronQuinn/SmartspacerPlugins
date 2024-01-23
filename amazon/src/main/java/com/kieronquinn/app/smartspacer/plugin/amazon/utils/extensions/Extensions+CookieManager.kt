package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.webkit.CookieManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun CookieManager.updateCookie(
    domain: String,
    key: String,
    value: String
) {
    setCookie("www.$domain", "$key=$value; Domain=.$domain")
}

suspend fun CookieManager.purge() = suspendCoroutine { resume ->
    removeAllCookies {
        resume.resume(it)
    }
}

fun CookieManager.getCookies(url: String): Map<String, String> {
    val cookieString = getCookie(url)
    return when {
        cookieString.isNullOrBlank() -> emptyList()
        !cookieString.contains(";") -> listOf(cookieString.trim())
        else -> {
            cookieString.split(";")
        }
    }.associate {
        it.parseCookie()
    }
}

fun String.parseCookie(): Pair<String, String> {
    val split = indexOf("=")
    val name = substring(0, split).trim()
    val value = substring(split + 1, length)
    return Pair(name, value)
}

fun Map<String, String>.toCookie(): String {
    return entries.joinToString(";") { "${it.key}=${it.value}"}
}