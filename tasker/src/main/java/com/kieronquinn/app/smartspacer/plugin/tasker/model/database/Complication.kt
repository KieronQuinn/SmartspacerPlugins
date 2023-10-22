package com.kieronquinn.app.smartspacer.plugin.tasker.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate

@Entity
data class Complication(
    @PrimaryKey
    @ColumnInfo("smartspacer_id")
    val smartspacerId: String,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("current")
    val current: ComplicationTemplate? = null,
    @ColumnInfo("refresh_period")
    val refreshPeriod: Int = 0,
    @ColumnInfo("refresh_if_not_visible")
    val refreshIfNotVisible: Boolean = false,
    @ColumnInfo("is_visible")
    val isVisible: Boolean = true,
    @ColumnInfo("updated_at")
    val updatedAt: Long? = null
) {

    companion object {
        fun createNewComplication(smartspacerId: String, name: String): Complication {
            return Complication(
                smartspacerId = smartspacerId,
                name = name
            )
        }
    }

}
