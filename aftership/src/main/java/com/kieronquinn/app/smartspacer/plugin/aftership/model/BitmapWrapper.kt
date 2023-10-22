package com.kieronquinn.app.smartspacer.plugin.aftership.model

import android.graphics.Bitmap
import com.kieronquinn.app.smartspacer.plugin.aftership.utils.extensions.writeToFile
import java.io.File

data class BitmapWrapper(
    val path: String,
    val bitmap: Bitmap?
) {

    companion object {
        fun create(path: String, bitmap: Bitmap): BitmapWrapper {
            bitmap.writeToFile(File(path))
            return BitmapWrapper(path, bitmap)
        }
    }

    fun delete() {
        File(path).let {
            if(it.exists()) it.delete()
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other !is BitmapWrapper) return false
        return other.path == path
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        return result
    }

}
