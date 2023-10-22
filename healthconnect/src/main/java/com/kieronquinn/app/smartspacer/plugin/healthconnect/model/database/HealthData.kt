package com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValue

@Entity
data class HealthData(
    @PrimaryKey
    @ColumnInfo("smartspacer_id")
    val smartspacerId: String,
    @ColumnInfo("authority")
    val authority: String,
    @ColumnInfo("has_permission")
    val hasPermission: Boolean,
    @ColumnInfo("value")
    val value: EncryptedValue?,
    @ColumnInfo("from_package")
    val fromPackage: EncryptedValue?,
    @ColumnInfo("was_rate_limited")
    val wasRateLimited: Boolean,
    @ColumnInfo("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)