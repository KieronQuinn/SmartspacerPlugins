package com.kieronquinn.app.smartspacer.plugin.shared.model.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RequirementDataDao {

    @Query("select * from `RequirementData`")
    fun getAll(): Flow<List<RequirementData>>

    @Query("select * from `RequirementData` where id=:id")
    fun getById(id: String): RequirementData?

    @Query("select * from `RequirementData` where id=:id")
    fun getByIdAsFlow(id: String): Flow<RequirementData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: RequirementData)

    @Update
    fun update(data: RequirementData)

    @Query("delete from `RequirementData` where id=:id")
    fun delete(id: String)

}