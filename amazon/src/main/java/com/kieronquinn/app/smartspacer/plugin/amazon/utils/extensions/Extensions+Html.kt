package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.text.Html

fun String.unescape(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()
}