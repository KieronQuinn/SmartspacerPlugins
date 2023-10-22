package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions


fun <T> List<T>.modify(index: Int, block: (T) -> T): List<T> {
    val current = getOrNull(index) ?: return this
    return replace(index, block(current))
}

private fun <T> List<T>.replace(index: Int, item: T): List<T> {
    return toMutableList().apply {
        this[index] = item
    }.toList()
}