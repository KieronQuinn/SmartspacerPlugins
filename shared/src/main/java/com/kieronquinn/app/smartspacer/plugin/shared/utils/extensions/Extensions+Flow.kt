package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend fun <T> Flow<T?>.firstNotNull(): T {
    return first { it != null }!!
}