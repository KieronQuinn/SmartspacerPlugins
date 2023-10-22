package com.kieronquinn.app.smartspacer.plugin.shared.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenCreated
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Fragment.childBackStackTopFragment() = callbackFlow {
    val listener = FragmentManager.OnBackStackChangedListener {
        trySend(getTopFragment())
    }
    childFragmentManager.addOnBackStackChangedListener(listener)
    trySend(getTopFragment())
    awaitClose {
        childFragmentManager.removeOnBackStackChangedListener(listener)
    }
}

fun Fragment.getTopFragment(): Fragment? {
    if(!isAdded) return null
    return childFragmentManager.fragments.firstOrNull()
}

/**
 *  Helper for [LifecycleOwner].[whenResumed]
 */
fun Fragment.whenResumed(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.whenResumed(block)
}

/**
 *  Helper for [LifecycleOwner].[whenCreated]
 */
fun Fragment.whenCreated(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.whenCreated(block)
}