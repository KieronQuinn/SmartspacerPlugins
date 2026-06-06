package com.kieronquinn.app.smartspacer.plugin.googlehealth.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.HealthMetric

@Entity
data class HealthFocus(
    @PrimaryKey
    @ColumnInfo(name = "metric")
    val metric: HealthMetric,
    @ColumnInfo("value")
    val value: String,
    @ColumnInfo("timestamp")
    val timestamp: Long
)