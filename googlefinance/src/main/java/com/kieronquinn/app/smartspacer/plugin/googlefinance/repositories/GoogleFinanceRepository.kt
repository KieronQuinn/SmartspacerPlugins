package com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.googlefinance.R
import com.kieronquinn.app.smartspacer.plugin.googlefinance.model.FinancialWidget
import com.kieronquinn.app.smartspacer.plugin.googlefinance.model.FinancialWidget.FinancialItem
import com.kieronquinn.app.smartspacer.plugin.googlefinance.targets.GoogleFinanceTarget
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

interface GoogleFinanceRepository {
    
    fun setFinancialWidget(
        smartspacerId: String,
        name: String,
        trend: String,
        pricePrefix: String,
        priceFirst: String,
        priceSecond: String,
        direction: Bitmap,
        priceTrend: Bitmap,
        closingPrice: Bitmap,
        items: List<FinancialItem>
    )

    fun getFinancialWidget(smartspacerId: String): FinancialWidget?
    fun deleteFinancialWidget(smartspacerId: String)
    
}

class GoogleFinanceRepositoryImpl(
    private val context: Context,
    private val gson: Gson
): GoogleFinanceRepository {

    private val scope = MainScope()

    private val financeDir = File(context.filesDir, "finance").apply {
        mkdirs()
    }

    override fun setFinancialWidget(
        smartspacerId: String,
        name: String,
        trend: String,
        pricePrefix: String,
        priceFirst: String,
        priceSecond: String,
        direction: Bitmap,
        priceTrend: Bitmap,
        closingPrice: Bitmap,
        items: List<FinancialItem>
    ) {
        scope.launch(Dispatchers.IO) {
            val accent = ContextCompat.getColor(context, R.color.accent)
            val background = ContextCompat.getColor(context, R.color.background)
            val chartBitmap = mergeChartBitmap(priceTrend, closingPrice).tint(accent, background)
            val item = FinancialWidget(
                name,
                trend,
                pricePrefix,
                priceFirst,
                priceSecond,
                direction,
                chartBitmap,
                items
            )
            val json = gson.toJson(item)
            File(financeDir, "$smartspacerId.json").writeText(json)
            SmartspacerTargetProvider.notifyChange(
                context, GoogleFinanceTarget::class.java, smartspacerId
            )
        }
    }

    override fun getFinancialWidget(smartspacerId: String): FinancialWidget? {
        val file = File(financeDir, "$smartspacerId.json")
        if(!file.exists()) return null
        return try {
            gson.fromJson(file.readText(), FinancialWidget::class.java)
        }catch (e: Exception){
            null
        }
    }

    override fun deleteFinancialWidget(smartspacerId: String) {
        val file = File(financeDir, "$smartspacerId.json")
        if(file.exists()) file.delete()
    }

    private fun Bitmap.tint(colour: Int, background: Int): Bitmap {
        return Bitmap.createBitmap(width, height, config).apply {
            val canvas = Canvas(this)
            canvas.drawColor(background)
            val paint = Paint()
            paint.colorFilter = PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(this@tint, 0f, 0f, paint)
        }.also {
            recycle()
        }
    }

    private fun mergeChartBitmap(priceTrend: Bitmap, closingPrice: Bitmap): Bitmap {
        val padding = 8.dp
        return Bitmap.createBitmap(
            priceTrend.width,
            priceTrend.height + padding + padding,
            Bitmap.Config.ARGB_8888
        ).apply {
            val canvas = Canvas(this)
            canvas.drawBitmap(priceTrend, 0f, padding.toFloat(), null)
            canvas.drawBitmap(closingPrice, 0f, padding.toFloat(), null)
        }.also {
            priceTrend.recycle()
            closingPrice.recycle()
        }
    }

}