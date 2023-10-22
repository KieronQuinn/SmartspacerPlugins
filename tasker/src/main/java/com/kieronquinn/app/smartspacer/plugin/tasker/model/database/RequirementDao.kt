package com.kieronquinn.app.smartspacer.plugin.tasker.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RequirementDao {

    @Query("select * from `Requirement`")
    fun getAll(): Flow<List<Requirement>>

    @Query("select * from `Requirement` where smartspacer_id=:smartspacerId limit 1")
    fun get(smartspacerId: String): Flow<Requirement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(target: Requirement)

    @Query("delete from `Requirement` where smartspacer_id=:smartspacerId")
    fun delete(smartspacerId: String)

}