package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import android.app.PendingIntent
import android.app.PendingIntentHidden
import android.content.Intent
import dev.rikka.tools.refine.Refine

fun PendingIntent.getIntent(): Intent {
    return Refine.unsafeCast<PendingIntentHidden>(this).intent
}