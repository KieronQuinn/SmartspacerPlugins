package com.kieronquinn.app.smartspacer.plugin.amazon.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AmazonDeliveryDao {

    @Query("select * from `AmazonDelivery`")
    fun getAll(): Flow<List<AmazonDelivery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(delivery: AmazonDelivery)

    @Query("delete from `AmazonDelivery` where id=:id")
    fun delete(id: String)

    @Query("delete from `AmazonDelivery`")
    fun clear()

}