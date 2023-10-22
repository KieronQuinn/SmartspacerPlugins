package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils

fun Int.toHexString(): String {
    return "#" + Integer.toHexString(this)
}

fun String.toColorOrNull(context: Context): Int? {
    return if(startsWith("#")) {
        parseHexColour()
    } else context.getSystemColourOrNull(this)
}

fun String.parseHexColour(): Int? {
    return try {
        Color.parseColor(this)
    }catch (e: IllegalAccessException){
        null
    }
}

@SuppressLint("DiscouragedApi")
private fun Context.getSystemColourOrNull(name: String): Int? {
    val resourceId = resources.getIdentifier(
        name, "color", "android"
    ).takeIf { it > 0 } ?: return null
    return try {
        ContextCompat.getColor(this, resourceId)
    }catch (e: Exception) {
        null
    }
}

fun Int.isColorDark(): Boolean {
    return ColorUtils.calculateLuminance(this) < 0.5
}