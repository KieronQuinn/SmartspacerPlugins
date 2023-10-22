package com.kieronquinn.app.smartspacer.plugins.pokemongo.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.core.graphics.scale


/**
 *  Trims a bitmap borders of a given color.
 *  https://stackoverflow.com/a/49281542/1088334
 */
fun Bitmap.trim(@ColorInt color: Int = Color.TRANSPARENT): Bitmap {
    var top = height
    var bottom = 0
    var right = width
    var left = 0

    var colored = IntArray(width) { color }
    var buffer = IntArray(width)

    for (y in bottom until top) {
        getPixels(buffer, 0, width, 0, y, width, 1)
        if (!colored.contentEquals(buffer)) {
            bottom = y
            break
        }
    }

    for (y in top - 1 downTo bottom) {
        getPixels(buffer, 0, width, 0, y, width, 1)
        if (!colored.contentEquals(buffer)) {
            top = y
            break
        }
    }

    val heightRemaining = top - bottom
    colored = IntArray(heightRemaining) { color }
    buffer = IntArray(heightRemaining)

    for (x in left until right) {
        getPixels(buffer, 0, 1, x, bottom, 1, heightRemaining)
        if (!colored.contentEquals(buffer)) {
            left = x
            break
        }
    }

    for (x in right - 1 downTo left) {
        getPixels(buffer, 0, 1, x, bottom, 1, heightRemaining)
        if (!colored.contentEquals(buffer)) {
            right = x
            break
        }
    }
    return Bitmap.createBitmap(this, left, bottom, right - left, top - bottom)
}

fun Bitmap.makeSquare(): Bitmap {
    val maxBound = width.coerceAtLeast(height)
    val left = (maxBound - width) / 2f
    val top = (maxBound - height) / 2f
    val destination = RectF(left, top, left + width, top + height)
    return Bitmap.createBitmap(maxBound, maxBound, Bitmap.Config.ARGB_8888).apply {
        val canvas = Canvas(this)
        canvas.drawBitmap(this@makeSquare, null, destination, null)
    }
}

fun Bitmap.resizeTo(size: Int): Bitmap {
    return scale(size, size)
}