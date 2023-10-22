package com.kieronquinn.app.smartspacer.plugins.bbcweather.model

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

data class TargetState(
    @SerializedName("location")
    val location: String,
    @SerializedName("icon")
    val icon: Bitmap,
    @SerializedName("content_description")
    val contentDescription: String,
    @SerializedName("temperature")
    val temperature: String,
    @SerializedName("items")
    val items: List<Item>
) {

    data class Item(
        @SerializedName("day")
        val day: String,
        @SerializedName("icon")
        val icon: Bitmap,
        @SerializedName("content_description")
        val contentDescription: String,
        @SerializedName("temperature")
        val temperature: String
    )

}
