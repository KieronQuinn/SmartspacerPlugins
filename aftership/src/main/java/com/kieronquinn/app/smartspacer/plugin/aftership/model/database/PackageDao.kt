package com.kieronquinn.app.smartspacer.plugin.aftership.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {

    @Query("select * from `Package`")
    fun getAll(): Flow<List<Package>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pkg: Package)

    @Query("delete from `Package` where id=:id")
    fun delete(id: String)

    @Query("delete from `Package`")
    fun clear()

}