package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import android.os.Build
import android.service.controls.Control

fun Control.isAuthRequiredCompat(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        isAuthRequired
    } else {
        true //Assume always required on < 14
    }
}