package com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValueConverter

@Database(entities = [
    ComplicationData::class,
    HealthData::class
], version = 1, exportSchema = false)
@TypeConverters(EncryptedValueConverter::class)
abstract class HealthConnectDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): HealthConnectDatabase {
            return Room.databaseBuilder(
                context,
                HealthConnectDatabase::class.java,
                "healthconnect"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao
    abstract fun healthDataDao(): HealthDataDao

}