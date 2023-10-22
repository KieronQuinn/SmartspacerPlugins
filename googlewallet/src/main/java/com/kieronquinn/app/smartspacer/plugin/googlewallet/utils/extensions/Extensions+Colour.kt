package com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions

import android.graphics.Color

fun com.google.type.Color.toColour(): Int {
    return if(hasAlpha()){
        Color.valueOf(red, green, blue, alpha.value)
    }else{
        Color.valueOf(red, green, blue)
    }.toArgb()
}