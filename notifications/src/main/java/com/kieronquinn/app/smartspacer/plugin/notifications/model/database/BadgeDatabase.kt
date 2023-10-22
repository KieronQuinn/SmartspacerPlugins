package com.kieronquinn.app.smartspacer.plugin.notifications.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    Badge::class
], version = 1, exportSchema = false)
abstract class BadgeDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): BadgeDatabase {
            return Room.databaseBuilder(
                context,
                BadgeDatabase::class.java,
                "badge"
            ).build()
        }
    }

    abstract fun badgeDao(): BadgeDao

}