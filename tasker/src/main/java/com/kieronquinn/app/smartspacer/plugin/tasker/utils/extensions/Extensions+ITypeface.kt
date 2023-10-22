package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.ITypeface

fun ITypeface.getIconOrNull(key: String): IIcon? {
    return try {
        getIcon(key)
    }catch (e: Exception) {
        null
    }
}