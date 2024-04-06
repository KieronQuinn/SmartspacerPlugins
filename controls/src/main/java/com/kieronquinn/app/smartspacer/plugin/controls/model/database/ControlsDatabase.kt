package com.kieronquinn.app.smartspacer.plugin.controls.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.RequirementData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.RequirementDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao

@Database(entities = [
    ComplicationData::class,
    TargetData::class,
    RequirementData::class
], version = 1, exportSchema = false)
@TypeConverters()
abstract class ControlsDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): ControlsDatabase {
            return Room.databaseBuilder(
                context,
                ControlsDatabase::class.java,
                "controls"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao
    abstract fun targetDataDao(): TargetDataDao
    abstract fun requirementDataDao(): RequirementDataDao

}