package com.kieronquinn.app.smartspacer.plugin.uber.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao

@Database(entities = [
    TargetData::class
], version = 1, exportSchema = false)
abstract class UberDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): UberDatabase {
            return Room.databaseBuilder(
                context,
                UberDatabase::class.java,
                "uber"
            ).build()
        }
    }

    abstract fun targetDataDao(): TargetDataDao

}