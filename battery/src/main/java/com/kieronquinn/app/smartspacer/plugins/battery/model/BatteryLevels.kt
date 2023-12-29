package com.kieronquinn.app.smartspacer.plugins.battery.model

import android.content.Context
import android.graphics.Bitmap
import com.kieronquinn.app.smartspacer.plugins.battery.R
import com.kieronquinn.app.smartspacer.plugins.battery.model.database.CachedBatteryLevel
import com.kieronquinn.app.smartspacer.plugins.battery.utils.extensions.writeToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.absoluteValue

data class BatteryLevels(
    val levels: List<BatteryLevel>
) {
    data class BatteryLevel(
        val name: String,
        val level: String,
        val isCharging: Boolean,
        val icon: Bitmap,
        val isConnected: Boolean
    ) {

        fun getLabel(context: Context): String {
            return if(isCharging){
                context.getString(R.string.complication_charging, level)
            }else level
        }

        suspend fun toCachedBatteryLevel(cacheDir: File): CachedBatteryLevel {
            val cacheFile = File(cacheDir, name.hashCode().absoluteValue.toString())
            withContext(Dispatchers.IO) {
                icon.writeToFile(cacheFile)
            }
            return CachedBatteryLevel(
                name,
                level,
                isCharging,
                BitmapWrapper(cacheFile.absolutePath, icon)
            )
        }

    }
}