package com.kieronquinn.app.smartspacer.plugin.uber.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale
import com.kieronquinn.app.smartspacer.plugin.uber.R
import com.kieronquinn.app.smartspacer.plugin.uber.model.NotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.utils.extensions.makeSquare
import com.kieronquinn.app.smartspacer.plugin.uber.utils.extensions.resize
import kotlin.math.atan2
import com.kieronquinn.app.shared.R as SharedR

private const val CANVAS_WIDTH = 768
private const val CANVAS_HEIGHT = 432
private const val CANVAS_PADDING = 64f
private const val CANVAS_ROUNDED_CORNERS = 32f
private const val OUTLINE_STROKE_WIDTH = 8f
private const val VEHICLE_SIZE = 96
private const val IMAGES_WIDTH = 312
private const val IMAGE_SIZE = 118
private const val FONT_BIGGER = 53f
private const val FONT_SMALLER = 40f

private val drawPaint = Paint().apply {
    strokeWidth = OUTLINE_STROKE_WIDTH
    strokeCap = Paint.Cap.ROUND
    style = Paint.Style.STROKE
}

private val fillPaint = Paint().apply {
    style = Paint.Style.FILL
}

private val textPaint = Paint().apply {
    textAlign = Paint.Align.CENTER
}

fun Context.drawNotificationData(
    data: NotificationData
): Bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888).apply {
    val canvas = Canvas(this)
    canvas.drawNotificationData(this@drawNotificationData, data)
}

private fun Canvas.drawNotificationData(
    context: Context,
    data: NotificationData
) {
    val font = context.getFont()
    val progressCar = data.progressCar.makeSquare().scale(VEHICLE_SIZE, VEHICLE_SIZE)
    progressCar.density = 0
    val driver = data.driver.scale(IMAGE_SIZE, IMAGE_SIZE)
    driver.density = 0
    val car = data.car.resize(IMAGES_WIDTH, IMAGE_SIZE)
    car.density = 0
    fillPaint.color = context.getBackground()
    drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)
    drawProgress(
        data.progress,
        progressCar,
        context.getUnfilledLineColour(),
        context.getFilledLineColour()
    )
    val imagesLeft = (width / 2f) - (car.width / 2f)
    val imagesTop = CANVAS_PADDING * 1.5f
    drawBitmap(car, imagesLeft, imagesTop, null)
    drawBitmap(driver, imagesLeft - 8f, imagesTop, null)
    val titleTop = imagesTop + car.height + 16f + FONT_BIGGER
    val subtitleTop = titleTop + 8f + FONT_SMALLER
    textPaint.typeface = Typeface.create(font, Typeface.BOLD)
    textPaint.color = context.getTextColour()
    textPaint.textSize = FONT_BIGGER
    drawText(data.expandedTitle, width / 2f, titleTop, textPaint)
    textPaint.typeface = font
    textPaint.textSize = FONT_SMALLER
    drawText(data.expandedSubtitle, width / 2f, subtitleTop, textPaint)
}

private fun Context.getBackground(): Int {
    return ContextCompat.getColor(this, R.color.colour_background)
}

private fun Context.getTextColour(): Int {
    return ContextCompat.getColor(this, R.color.colour_text)
}

private fun Context.getFilledLineColour(): Int {
    return ContextCompat.getColor(this, R.color.progress_bar_filled)
}

private fun Context.getUnfilledLineColour(): Int {
    return ContextCompat.getColor(this, R.color.progress_bar_unfilled)
}

private fun Context.getFont(): Typeface? {
    return ResourcesCompat.getFont(this, SharedR.font.google_sans_text)
}

private fun Canvas.drawProgress(
    end: Float,
    car: Bitmap,
    unfilledColour: Int,
    filledColour: Int
) {
    save()
    translate(0f, height.toFloat())
    scale(1f, -1f)
    val startOffset = -0.03f
    val endOffset = 0.01f
    val offsetEnd = 1f - end.coerceAtLeast(0.05f)
    drawPaint.color = unfilledColour
    drawPath(createRoundedPath(0f + endOffset, 1f + startOffset), drawPaint)
    drawPaint.color = filledColour
    val progressPath = createRoundedPath(offsetEnd + endOffset, 1f + startOffset)
    drawPath(progressPath, drawPaint)
    val carPosition = progressPath.getVehiclePosition()
    drawBitmap(car, carPosition, null)
    restore()
}

private fun createRoundedPath(start: Float, end: Float): Path {
    val roundedRectPath = Path()
    roundedRectPath.addRoundRect(
        CANVAS_PADDING,
        CANVAS_PADDING,
        CANVAS_WIDTH - CANVAS_PADDING,
        CANVAS_HEIGHT - CANVAS_PADDING,
        CANVAS_ROUNDED_CORNERS,
        CANVAS_ROUNDED_CORNERS,
        Path.Direction.CW
    )
    val pathMeasure = PathMeasure(roundedRectPath, true)
    return Path().apply {
        val startD = start * pathMeasure.length
        val stopD = end * pathMeasure.length
        pathMeasure.getSegment(startD, stopD, this, true)
    }
}

private fun Path.getVehiclePosition() = Matrix().apply {
    val pathMeasure = PathMeasure(this@getVehiclePosition, true)
    val rotationMatrix = Matrix()
    val tan = floatArrayOf(0f, 0f)
    val pos = floatArrayOf(0f, 0f)
    pathMeasure.getPosTan(1f, pos, tan)
    pathMeasure.getMatrix(1f, rotationMatrix, 0)
    val degrees = atan2(tan[1], tan[0]) * 180.0 / Math.PI
    val vehiclePivot = VEHICLE_SIZE / 2f
    postRotate(degrees.toFloat() + 180f, vehiclePivot, vehiclePivot)
    postTranslate(pos[0] - vehiclePivot, pos[1] - vehiclePivot)
}