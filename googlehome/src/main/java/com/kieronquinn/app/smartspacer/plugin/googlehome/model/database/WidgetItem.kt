package com.kieronquinn.app.smartspacer.plugin.googlehome.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WidgetItem(
    @PrimaryKey
    @ColumnInfo(name = "smartspacer_id")
    val smartspacerId: String,
    @ColumnInfo("data")
    val data: String
)