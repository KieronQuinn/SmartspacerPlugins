package com.kieronquinn.app.smartspacer.plugins.yahoosport.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao

@Database(entities = [
    TargetData::class
], version = 1, exportSchema = false)
abstract class YahooSportDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): YahooSportDatabase {
            return Room.databaseBuilder(
                context,
                YahooSportDatabase::class.java,
                "yahoo_sport"
            ).build()
        }
    }

    abstract fun targetDataDao(): TargetDataDao

}