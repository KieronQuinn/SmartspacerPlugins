package com.kieronquinn.app.smartspacer.plugin.googlehome.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetItemDao {

    @Query("select * from WidgetItem")
    fun getAll(): Flow<List<WidgetItem>>

    @Query("delete from WidgetItem where smartspacer_id = :smartspacerId")
    fun delete(smartspacerId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: WidgetItem)

}