package com.kieronquinn.app.smartspacer.plugin.shared.model.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplicationDataDao {

    @Query("select * from `ComplicationData`")
    fun getAll(): Flow<List<ComplicationData>>

    @Query("select * from `ComplicationData` where id=:id")
    fun getById(id: String): ComplicationData?

    @Query("select * from `ComplicationData` where id=:id")
    fun getByIdAsFlow(id: String): Flow<ComplicationData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: ComplicationData)

    @Update
    fun update(data: ComplicationData)

    @Query("delete from `ComplicationData` where id=:id")
    fun delete(id: String)

}