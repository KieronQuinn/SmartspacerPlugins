package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.webkit.CookieManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun CookieManager.removeAllCookies() = suspendCoroutine<Boolean> {
    removeAllCookies { result ->
        it.resume(result)
    }
}