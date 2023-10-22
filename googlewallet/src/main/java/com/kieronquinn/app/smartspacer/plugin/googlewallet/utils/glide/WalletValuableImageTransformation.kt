package com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.scale
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.getRoundedBitmap
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.glide.WalletValuableImageTransformation.Companion.OUT_HEIGHT
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.glide.WalletValuableImageTransformation.Companion.OUT_WIDTH
import java.security.MessageDigest

/**
 *  Creates a Bitmap of size [OUT_WIDTH]x[OUT_HEIGHT] with the Valuable's logo overlayed in the
 *  center, outlined with a grey ring.
 */
class WalletValuableImageTransformation(
    private val backgroundColour: Int
): BitmapTransformation() {

    companion object {
        private val ID = WalletValuableImageTransformation::class.java.name
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)

        private val OUTLINE_COLOUR = Color.GRAY
        private const val OUT_WIDTH = 910
        private const val OUT_HEIGHT = 512
        private const val SCALED_SIZE = 400
        private const val DRAW_X = 255f
        private const val DRAW_Y = 56f
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val roundedBitmap = toTransform.getRoundedBitmap()
            .scale(SCALED_SIZE, SCALED_SIZE, true)
        val paint = Paint().apply {
            isAntiAlias = true
            color = OUTLINE_COLOUR
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val cardBitmap = Bitmap.createBitmap(OUT_WIDTH, OUT_HEIGHT, Bitmap.Config.ARGB_8888)
        val cardCanvas = Canvas(cardBitmap)
        cardCanvas.drawColor(backgroundColour)
        cardCanvas.drawBitmap(roundedBitmap, DRAW_X, DRAW_Y, paint)
        cardCanvas.drawCircle(OUT_WIDTH / 2f, OUT_HEIGHT / 2f, SCALED_SIZE / 2f, paint)
        return cardBitmap
    }

    override fun equals(other: Any?): Boolean {
        return other is WalletValuableImageTransformation
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

}