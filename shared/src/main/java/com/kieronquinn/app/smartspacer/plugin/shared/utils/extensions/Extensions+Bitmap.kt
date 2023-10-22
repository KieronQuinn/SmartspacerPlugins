package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import android.graphics.Bitmap

fun Bitmap_createBlankBitmap(): Bitmap {
    return Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
}