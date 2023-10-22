package com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions

import android.text.Html
import com.google.internal.tapandpay.v1.passes.templates.ReferenceProto.ReferenceValue

fun ReferenceValue.asString(): CharSequence? {
    val value = when(valueCase) {
        ReferenceValue.ValueCase.HTML -> Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        ReferenceValue.ValueCase.SAFE_HTML -> Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        ReferenceValue.ValueCase.DYNAMIC_FORMATTED_STRING -> dynamicFormattedString
        else -> null
    }
    return value?.ifEmpty { null } ?: rawValue.ifEmpty { null }
}