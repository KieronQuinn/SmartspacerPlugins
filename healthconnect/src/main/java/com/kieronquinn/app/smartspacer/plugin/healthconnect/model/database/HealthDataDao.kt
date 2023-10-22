package com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDataDao {

    @Query("select * from `HealthData`")
    fun getHealthData(): Flow<List<HealthData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setHealthData(healthData: HealthData)

    @Query("delete from `HealthData` where smartspacer_id=:smartspacerId")
    fun deleteHealthData(smartspacerId: String)

}