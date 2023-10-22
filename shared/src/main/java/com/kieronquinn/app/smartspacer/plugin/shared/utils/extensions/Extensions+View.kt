package com.kieronquinn.app.smartspacer.plugin.shared.utils

import android.view.View
import android.view.ViewHidden
import android.view.ViewRootImpl
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.rikka.tools.refine.Refine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val TAP_DEBOUNCE = 250L

fun View.getViewRootImpl(): ViewRootImpl? {
    return Refine.unsafeCast<ViewHidden>(this).viewRootImpl
}

suspend fun View.awaitPost() = suspendCancellableCoroutine {
    post {
        if(isAttachedToWindow){
            it.resume(this)
        }else{
            it.cancel()
        }
    }
}

fun View.onClicked() = callbackFlow {
    setOnClickListener {
        trySend(it)
    }
    awaitClose {
        setOnClickListener(null)
    }
}.debounce(TAP_DEBOUNCE)

fun View.onLongClicked() = callbackFlow<View> {
    setOnLongClickListener {
        trySend(it)
        true
    }
    awaitClose {
        setOnClickListener(null)
    }
}.debounce(TAP_DEBOUNCE)

fun View.hideIme() {
    ViewCompat.getWindowInsetsController(this)?.hide(WindowInsetsCompat.Type.ime())
}