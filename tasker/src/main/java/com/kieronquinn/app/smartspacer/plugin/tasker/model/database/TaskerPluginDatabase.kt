package com.kieronquinn.app.smartspacer.plugin.tasker.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.tasker.converters.ComplicationTemplateConverter
import com.kieronquinn.app.smartspacer.plugin.tasker.converters.TargetTemplateConverter

@Database(entities = [
    Complication::class,
    Target::class,
    Requirement::class
], version = 1, exportSchema = false)
@TypeConverters(
    TargetTemplateConverter::class,
    ComplicationTemplateConverter::class
)
abstract class TaskerPluginDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): TaskerPluginDatabase {
            return Room.databaseBuilder(
                context,
                TaskerPluginDatabase::class.java,
                "tasker_plugin"
            ).build()
        }
    }

    abstract fun targetDao(): TargetDao
    abstract fun complicationDao(): ComplicationDao
    abstract fun requirementDao(): RequirementDao

}