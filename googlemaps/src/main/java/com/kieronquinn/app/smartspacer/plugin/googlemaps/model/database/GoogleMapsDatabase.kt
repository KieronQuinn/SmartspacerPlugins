package com.kieronquinn.app.smartspacer.plugin.googlemaps.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao

@Database(entities = [
    TargetData::class
], version = 1, exportSchema = false)
@TypeConverters()
abstract class GoogleMapsDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): GoogleMapsDatabase {
            return Room.databaseBuilder(
                context,
                GoogleMapsDatabase::class.java,
                "maps"
            ).build()
        }
    }

    abstract fun targetDataDao(): TargetDataDao

}