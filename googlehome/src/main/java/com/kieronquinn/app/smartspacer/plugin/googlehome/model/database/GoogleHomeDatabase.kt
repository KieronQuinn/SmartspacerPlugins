package com.kieronquinn.app.smartspacer.plugin.googlehome.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao

@Database(entities = [
    ComplicationData::class,
    TargetData::class,
    WidgetItem::class
], version = 1, exportSchema = false)
abstract class GoogleHomeDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): GoogleHomeDatabase {
            return Room.databaseBuilder(
                context,
                GoogleHomeDatabase::class.java,
                "googlehome"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao
    abstract fun targetDataDao(): TargetDataDao
    abstract fun widgetItemDao(): WidgetItemDao

}