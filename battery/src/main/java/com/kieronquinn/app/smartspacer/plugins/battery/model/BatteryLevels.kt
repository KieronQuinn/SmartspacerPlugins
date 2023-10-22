package com.kieronquinn.app.smartspacer.plugins.battery.model

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugins.battery.R

data class BatteryLevels(
    @SerializedName("levels")
    val levels: List<BatteryLevel>
) {
    data class BatteryLevel(
        @SerializedName("name")
        val name: String,
        @SerializedName("level")
        val level: String,
        @SerializedName("is_charging")
        val isCharging: Boolean,
        @SerializedName("icon")
        val icon: Bitmap
    ) {

        fun getLabel(context: Context): String {
            return if(isCharging){
                context.getString(R.string.complication_charging, level)
            }else level
        }

    }
}