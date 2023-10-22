package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

fun AppBarLayout.collapsedState() = callbackFlow {
    var cachedState = false
    val listener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
        if(verticalOffset == 0){
            if(cachedState) {
                cachedState = false
                trySend(false)
            }
        }else if(abs(verticalOffset) >= totalScrollRange){
            if(!cachedState) {
                cachedState = true
                trySend(true)
            }
        }
    }
    addOnOffsetChangedListener(listener)
    awaitClose {
        removeOnOffsetChangedListener(listener)
    }
}

fun AppBarLayout.expandProgress() = callbackFlow {
    val listener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
        val progress = (verticalOffset / totalScrollRange.toFloat())
        trySend(1 - abs(progress))
    }
    addOnOffsetChangedListener(listener)
    awaitClose {
        removeOnOffsetChangedListener(listener)
    }
}

private const val FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED = "app_bar_collapsed"

fun Fragment.rememberAppBarCollapsed(
    collapsed: Boolean,
    key: String = FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED
) {
    val arguments = this.arguments ?: Bundle()
    arguments.putBoolean(key, collapsed)
    this.arguments = arguments
}

fun Fragment.getRememberedAppBarCollapsed(
    key: String = FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED
): Boolean {
    return arguments?.getBoolean(key, false) ?: false
}