package com.kieronquinn.app.smartspacer.plugin.shared.model.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TargetDataDao {

    @Query("select * from `TargetData`")
    fun getAll(): Flow<List<TargetData>>

    @Query("select * from `TargetData` where id=:id")
    fun getById(id: String): TargetData?

    @Query("select * from `TargetData` where id=:id")
    fun getByIdAsFlow(id: String): Flow<TargetData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: TargetData)

    @Update
    fun update(data: TargetData)

    @Query("delete from `TargetData` where id=:id")
    fun delete(id: String)

}