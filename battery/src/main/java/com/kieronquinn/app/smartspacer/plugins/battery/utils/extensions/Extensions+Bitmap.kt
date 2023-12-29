package com.kieronquinn.app.smartspacer.plugins.battery.utils.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import java.io.File

fun Bitmap.writeToFile(file: File) {
    file.parentFile?.mkdirs()
    file.outputStream().use {
        compress(Bitmap.CompressFormat.PNG, 100, it)
        it.flush()
    }
}

fun Bitmap.makeSquare(): Bitmap {
    val maxSize = maxOf(width, height)
    return Bitmap.createBitmap(maxSize, maxSize, config).apply {
        val canvas = Canvas(this)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(
            this@makeSquare,
            (maxSize - this@makeSquare.width) / 2f,
            (maxSize - this@makeSquare.height) / 2f,
            null
        )
        this@makeSquare.recycle()
    }
}