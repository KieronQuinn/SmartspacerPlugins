package com.kieronquinn.app.smartspacer.plugin.tasker.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate

@Entity
data class Target(
    @PrimaryKey
    @ColumnInfo("smartspacer_id")
    val smartspacerId: String,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("current")
    val current: TargetTemplate? = null,
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
        fun createNewTarget(smartspacerId: String, name: String): Target {
            return Target(
                smartspacerId = smartspacerId,
                name = name
            )
        }
    }

}
