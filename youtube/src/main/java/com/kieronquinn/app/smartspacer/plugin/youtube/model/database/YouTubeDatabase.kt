package com.kieronquinn.app.smartspacer.plugin.youtube.model.database

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
abstract class YouTubeDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): YouTubeDatabase {
            return Room.databaseBuilder(
                context,
                YouTubeDatabase::class.java,
                "youtube"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao

}