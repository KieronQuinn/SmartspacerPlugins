package com.kieronquinn.app.smartspacer.plugin.notifications.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("select * from `Badge`")
    fun getBadges(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBadge(badge: Badge)

}