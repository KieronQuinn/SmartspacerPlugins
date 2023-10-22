package com.kieronquinn.app.smartspacer.plugin.uber.utils.extensions

import android.graphics.Bitmap
import android.graphics.Canvas

fun Bitmap.makeSquare(): Bitmap {
    val maxSize = maxOf(width, height)
    return Bitmap.createBitmap(maxSize, maxSize, config).apply {
        val canvas = Canvas(this)
        canvas.drawBitmap(
            this@makeSquare,
            (maxSize - this@makeSquare.width) / 2f,
            (maxSize - this@makeSquare.height) / 2f,
            null
        )
        this@makeSquare.recycle()
    }
}

fun Bitmap.resize(maxWidth: Int, maxHeight: Int): Bitmap {
    return if (maxHeight > 0 && maxWidth > 0) {
        val width = this.width
        val height = this.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        Bitmap.createScaledBitmap(this, finalWidth, finalHeight, true)
    } else this
}