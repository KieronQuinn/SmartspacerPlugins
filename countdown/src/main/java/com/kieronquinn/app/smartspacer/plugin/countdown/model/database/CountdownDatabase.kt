package com.kieronquinn.app.smartspacer.plugin.countdown.model.database

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
abstract class CountdownDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): CountdownDatabase {
            return Room.databaseBuilder(
                context,
                CountdownDatabase::class.java,
                "countdown"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao

}