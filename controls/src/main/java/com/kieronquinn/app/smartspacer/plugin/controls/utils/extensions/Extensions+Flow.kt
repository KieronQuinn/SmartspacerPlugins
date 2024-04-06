package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 *  Equivalent to [Flow.timeout] but only applies to the first emission. After this, the flow will
 *  behave as normal. Emits `null` on timeout.
 */
fun <T> Flow<T?>.timeoutFirst(timeout: Long) = flow {
    val first = withTimeoutOrNull(timeout) {
        first()
    }
    emit(first)
    if(first == null) return@flow
    this@timeoutFirst.collect {
        emit(it)
    }
}