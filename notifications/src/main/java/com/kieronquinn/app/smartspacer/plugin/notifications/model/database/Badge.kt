package com.kieronquinn.app.smartspacer.plugin.notifications.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Badge(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "badge_count")
    val badgeCount: Int
)