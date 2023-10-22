package com.kieronquinn.app.smartspacer.plugin.aftership.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.aftership.utils.room.BitmapWrapperConverter
import com.kieronquinn.app.smartspacer.plugin.aftership.utils.room.TrackingConverter
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValueConverter

@Database(entities = [
    TargetData::class,
    Package::class
], version = 1, exportSchema = false)
@TypeConverters(EncryptedValueConverter::class, BitmapWrapperConverter::class, TrackingConverter::class)
abstract class AftershipDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): AftershipDatabase {
            return Room.databaseBuilder(
                context,
                AftershipDatabase::class.java,
                "aftership"
            ).build()
        }
    }

    abstract fun packageDao(): PackageDao
    abstract fun targetDataDao(): TargetDataDao

}