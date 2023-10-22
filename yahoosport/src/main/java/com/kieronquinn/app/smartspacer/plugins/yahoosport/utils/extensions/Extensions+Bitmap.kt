package com.kieronquinn.app.smartspacer.plugins.yahoosport.utils.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap

fun Bitmap.addPadding(paddingAmount: Float): Bitmap {
    val paddingSize = width + (width * paddingAmount).toInt()
    val start = (paddingSize / 2f) - (width / 2f)
    return createBitmap(paddingSize, paddingSize, config).apply {
        val canvas = Canvas(this)
        canvas.drawBitmap(this@addPadding, start, start, null)
    }.also {
        recycle()
    }
}