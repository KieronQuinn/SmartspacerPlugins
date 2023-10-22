package com.kieronquinn.app.smartspacer.plugins.bbcweather.model

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.sdk.model.weather.WeatherData.WeatherStateIcon

data class ComplicationState(
    @SerializedName("icon")
    val icon: Bitmap,
    @SerializedName("temperature")
    val temperature: String,
    @SerializedName("content_description")
    val contentDescription: String,
    @SerializedName("weather_state_icon")
    private val weatherState: String?
) {

    val weatherStateIcon
        get() = weatherState?.let { state ->
            WeatherStateIcon.values().firstOrNull {
                it.name == state
            }
        }

}
