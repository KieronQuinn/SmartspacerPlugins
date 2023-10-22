package com.kieronquinn.app.smartspacer.plugin.googlefinance.model

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

data class FinancialWidget(
    @SerializedName("name")
    val name: String,
    @SerializedName("trend")
    val trend: String,
    @SerializedName("price_prefix")
    val pricePrefix: String,
    @SerializedName("price_first")
    val priceFirst: String,
    @SerializedName("price_second")
    val priceSecond: String,
    @SerializedName("direction")
    val direction: Bitmap,
    @SerializedName("chart")
    val chart: Bitmap,
    @SerializedName("items")
    val financialItems: List<FinancialItem>
) {

    fun getHash(): Int {
        var result = name.hashCode()
        result = 31 * result + trend.hashCode()
        return result
    }

    data class FinancialItem(
        @SerializedName("name")
        val name: String,
        @SerializedName("trend")
        val trend: String,
        @SerializedName("price_first")
        val priceFirst: String,
        @SerializedName("price_second")
        val priceSecond: String,
        @SerializedName("direction")
        val direction: Bitmap
    )

}