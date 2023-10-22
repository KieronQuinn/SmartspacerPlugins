package com.kieronquinn.app.smartspacer.plugin.samsunghealth.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao

@Database(entities = [
    ComplicationData::class
], version = 1, exportSchema = false)
abstract class SamsungHealthDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): SamsungHealthDatabase {
            return Room.databaseBuilder(
                context,
                SamsungHealthDatabase::class.java,
                "samsunghealth"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao

}