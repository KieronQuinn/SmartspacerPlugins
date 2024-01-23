package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

fun <T> Collection<T>.takeIfNotEmpty(): Collection<T>? {
    return takeIf { it.isNotEmpty() }
}