package com.kieronquinn.app.smartspacer.plugin.googlehealth.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthFocusDao {

    @Query("select * from HealthFocus")
    fun getAll(): Flow<List<HealthFocus>>

    @Query("delete from HealthFocus")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: List<HealthFocus>)

}