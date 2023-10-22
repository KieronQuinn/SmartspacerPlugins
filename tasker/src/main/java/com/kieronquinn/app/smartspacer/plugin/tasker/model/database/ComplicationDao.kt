package com.kieronquinn.app.smartspacer.plugin.tasker.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplicationDao {

    @Query("select * from `Complication`")
    fun getAll(): Flow<List<Complication>>

    @Query("select * from `Complication` where smartspacer_id=:smartspacerId limit 1")
    fun get(smartspacerId: String): Flow<Complication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(complication: Complication)

    @Query("delete from `Complication` where smartspacer_id=:smartspacerId")
    fun delete(smartspacerId: String)

}