package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.utils.extensions

import android.content.Context
import android.content.pm.PackageManager

fun Context.hasPermissions(vararg permission: String): Boolean {
    return permission.all {
        checkCallingOrSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }
}