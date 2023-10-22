package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

fun String.countOf(other: String): Int {
    return split(other).size - 1
}