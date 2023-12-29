package com.kieronquinn.app.smartspacer.plugins.battery.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao
import com.kieronquinn.app.smartspacer.plugins.battery.utils.room.BitmapWrapperConverter

@Database(entities = [
    ComplicationData::class,
    CachedBatteryLevel::class
], version = 2, exportSchema = false)
@TypeConverters(BitmapWrapperConverter::class)
abstract class BatteryDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): BatteryDatabase {
            return Room.databaseBuilder(
                context,
                BatteryDatabase::class.java,
                "battery"
            ).addMigrations(MIGRATION_1_2).build()
        }

        private val MIGRATION_1_2 = object: Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("create table if not exists CachedBatteryLevel ( name TEXT PRIMARY KEY NOT NULL, level TEXT NOT NULL, is_charging INTEGER NOT NULL, icon TEXT NOT NULL, is_connected INTEGER NOT NULL );")
            }
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao
    abstract fun cachedBatteryLevelDao(): CachedBatteryLevelDao

}