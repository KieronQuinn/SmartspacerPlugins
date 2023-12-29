package com.kieronquinn.app.smartspacer.plugins.battery.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedBatteryLevelDao {

    @Query("select * from CachedBatteryLevel")
    fun getAll(): Flow<List<CachedBatteryLevel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(cachedBatteryLevel: CachedBatteryLevel)

    @Query("update CachedBatteryLevel set is_connected=:isConnected where name=:name")
    suspend fun setConnected(name: String, isConnected: Boolean)

    @Query("delete from CachedBatteryLevel where name=:name")
    suspend fun delete(name: String)

}