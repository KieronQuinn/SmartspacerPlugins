package com.kieronquinn.app.smartspacer.plugin.googlehealth.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao

@Database(entities = [
    ComplicationData::class,
    HealthFocus::class
], version = 1, exportSchema = false)
abstract class GoogleHealthDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): GoogleHealthDatabase {
            return Room.databaseBuilder(
                context,
                GoogleHealthDatabase::class.java,
                "googlehealth"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao
    abstract fun healthFocusDao(): HealthFocusDao

}