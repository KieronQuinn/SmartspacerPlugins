package com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions

import com.google.internal.tapandpay.v1.valuables.CommonProto.LabeledStringValue

fun LabeledStringValue.merge(): String? {
    return takeIf { label.isNotEmpty() && value.isNotBlank() }?.let {
        "$label $value"
    }
}