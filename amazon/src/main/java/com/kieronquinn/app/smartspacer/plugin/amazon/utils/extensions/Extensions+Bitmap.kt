package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValue
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValueConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * Compresses the bitmap to a byte array for serialization.
 */
fun Bitmap.compress(): ByteArray? {
    val out = ByteArrayOutputStream(getExpectedBitmapSize())
    return try {
        compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        out.toByteArray()
    } catch (e: IOException) {
        null
    }
}

/**
 * Try go guesstimate how much space the icon will take when serialized to avoid unnecessary
 * allocations/copies during the write (4 bytes per pixel).
 */
private fun Bitmap.getExpectedBitmapSize(): Int {
    return width * height * 4
}

fun Context.deleteEncryptedBitmaps(shipmentId: String) {
    ImageType.values().forEach {
        val file = getImageFile(shipmentId, it)
        if(file.exists()) {
            file.delete()
        }
    }
}

suspend fun Context.writeEncryptedBitmap(shipmentId: String, type: ImageType, bitmap: Bitmap) {
    withContext(Dispatchers.IO) {
        val compressedBitmap = bitmap.compress() ?: return@withContext
        val encryptedValue = EncryptedValue(compressedBitmap)
        val encrypted = EncryptedValueConverter.fromEncryptedValue(encryptedValue)
            ?: return@withContext
        getImageFile(shipmentId, type).writeBytes(encrypted)
    }
}

suspend fun Context.readEncryptedBitmap(shipmentId: String, type: ImageType): Bitmap? {
    return withContext(Dispatchers.IO) {
        val encryptedFile = getImageFile(shipmentId, type)
        if(!encryptedFile.exists()) return@withContext null
        val decrypted = EncryptedValueConverter.fromBytes(encryptedFile.readBytes())?.bytes
            ?: return@withContext null
        BitmapFactory.decodeByteArray(decrypted, 0, decrypted.size)
    }
}

private fun Context.getImageFile(shipmentId: String, type: ImageType): File {
    return File(filesDir, "${shipmentId}_${type.type}.enc.png")
}

fun Context.clearEncryptedBitmaps() {
    filesDir.listFiles()?.filter { it.absolutePath.endsWith(".enc.png") }?.forEach {
        it.delete()
    }
}

enum class ImageType(val type: String) {
    IMAGE("image"), MAP("map")
}