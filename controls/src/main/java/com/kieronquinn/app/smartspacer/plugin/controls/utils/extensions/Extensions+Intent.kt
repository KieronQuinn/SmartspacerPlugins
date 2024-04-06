package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentHidden
import android.os.Binder
import androidx.core.os.bundleOf
import dev.rikka.tools.refine.Refine

fun Intent.prepareToLeaveProcess(context: Context) {
    Refine.unsafeCast<IntentHidden>(this).prepareToLeaveProcess(context)
}

fun Intent.resolveService(context: Context): ComponentName? {
    return context.packageManager.resolveService(this, 0)?.serviceInfo?.let {
        ComponentName(it.packageName, it.name)
    }
}

fun Intent.makeExplicitServiceIntent(context: Context): Intent = apply {
    context.packageManager.resolveService(this, 0)?.serviceInfo?.let {
        component = ComponentName(it.packageName, it.name)
    }
}

const val ACTION_CONTROLS_PROVIDER = "android.service.controls.ControlsProviderService"

private const val KEY_CALLBACK_BUNDLE = "CALLBACK_BUNDLE"
private const val KEY_CALLBACK_TOKEN = "CALLBACK_TOKEN"

fun Intent.makeControlsIntent(): Intent {
    return Intent(this).apply {
        putExtra(KEY_CALLBACK_BUNDLE, bundleOf(KEY_CALLBACK_TOKEN to Binder()))
    }
}