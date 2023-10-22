package com.kieronquinn.app.smartspacer.plugin.googlefinance.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao

@Database(entities = [
    TargetData::class
], version = 1, exportSchema = false)
abstract class GoogleFinanceDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): GoogleFinanceDatabase {
            return Room.databaseBuilder(
                context,
                GoogleFinanceDatabase::class.java,
                "googlefinance"
            ).build()
        }
    }

    abstract fun targetDataDao(): TargetDataDao

}