package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Context.loadImageUrl(imageUrl: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        Glide.with(this@loadImageUrl).asBitmap().load(imageUrl).submit().get()
    }
}