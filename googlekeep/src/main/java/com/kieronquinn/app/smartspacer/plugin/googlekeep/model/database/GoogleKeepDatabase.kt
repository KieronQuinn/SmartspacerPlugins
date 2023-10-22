package com.kieronquinn.app.smartspacer.plugin.googlekeep.model.database

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
abstract class GoogleKeepDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): GoogleKeepDatabase {
            return Room.databaseBuilder(
                context,
                GoogleKeepDatabase::class.java,
                "keep"
            ).build()
        }
    }

    abstract fun targetDataDao(): TargetDataDao

}