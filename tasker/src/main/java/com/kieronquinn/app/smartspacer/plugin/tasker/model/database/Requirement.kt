package com.kieronquinn.app.smartspacer.plugin.tasker.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Requirement(
    @PrimaryKey
    @ColumnInfo("smartspacer_id")
    val smartspacerId: String,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("is_met")
    val isMet: Boolean = false,
    @ColumnInfo("updated_at")
    val updatedAt: Long? = null
) {

    companion object {
        fun createNewRequirement(smartspacerId: String, name: String): Requirement {
            return Requirement(
                smartspacerId = smartspacerId,
                name = name
            )
        }
    }

}