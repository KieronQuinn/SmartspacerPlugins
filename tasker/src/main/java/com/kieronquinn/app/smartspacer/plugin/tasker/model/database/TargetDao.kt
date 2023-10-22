package com.kieronquinn.app.smartspacer.plugin.tasker.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TargetDao {

    @Query("select * from `Target`")
    fun getAll(): Flow<List<Target>>

    @Query("select * from `Target` where smartspacer_id=:smartspacerId limit 1")
    fun get(smartspacerId: String): Flow<Target>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(target: Target)

    @Query("delete from `Target` where smartspacer_id=:smartspacerId")
    fun delete(smartspacerId: String)

}