package com.kieronquinn.app.smartspacer.plugins.datausage.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao

@Database(entities = [
    ComplicationData::class
], version = 1, exportSchema = false)
@TypeConverters
abstract class DataUsageDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): DataUsageDatabase {
            return Room.databaseBuilder(
                context,
                DataUsageDatabase::class.java,
                "datausage"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao

}