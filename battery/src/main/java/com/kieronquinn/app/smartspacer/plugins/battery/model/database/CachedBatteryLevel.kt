package com.kieronquinn.app.smartspacer.plugins.battery.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.smartspacer.plugins.battery.model.BatteryLevels.BatteryLevel
import com.kieronquinn.app.smartspacer.plugins.battery.model.BitmapWrapper

@Entity
data class CachedBatteryLevel(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "level")
    val level: String,
    @ColumnInfo(name = "is_charging")
    val isCharging: Boolean,
    @ColumnInfo(name = "icon")
    val icon: BitmapWrapper,
    @ColumnInfo(name = "is_connected")
    val isConnected: Boolean = true
) {

    fun toBatteryLevel(): BatteryLevel? {
        return BatteryLevel(name, level, isCharging, icon.bitmap ?: return null, isConnected)
    }

}