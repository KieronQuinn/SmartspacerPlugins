package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface

fun UiSurface.describe(context: Context): String? {
    return when(this) {
        UiSurface.HOMESCREEN -> R.string.ui_surface_home
        UiSurface.LOCKSCREEN -> R.string.ui_surface_lock
        else -> return null
    }.let {
        context.getString(it)
    }
}

fun UiSurface_validSurfaces(): List<UiSurface> {
    return UiSurface.values().filterNot { it == UiSurface.MEDIA_DATA_MANAGER }
}